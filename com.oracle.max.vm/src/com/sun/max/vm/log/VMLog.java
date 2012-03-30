/*
 * Copyright (c) 2012, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package com.sun.max.vm.log;

import java.util.*;

import com.sun.max.annotate.*;
import com.sun.max.program.*;
import com.sun.max.unsafe.*;
import com.sun.max.vm.*;
import com.sun.max.vm.actor.holder.*;
import com.sun.max.vm.heap.*;
import com.sun.max.vm.heap.Heap.*;
import com.sun.max.vm.hosted.*;
import com.sun.max.vm.log.hosted.*;
import com.sun.max.vm.reference.*;
import com.sun.max.vm.thread.*;

/**
 * A circular buffer of logged VM operations.
 * A logged VM operation is defined by a {@link VMLogger} instance, which
 * is typically associated with some component of the VM,and registered
 * with the {@link #registerLogger(VMLogger)} method.
 *
 * A variety of implementations of the log buffer are possible,
 * varying in performance, space overhead and complexity.
 * To allow experimentation, a specific logger is chosen by a factory class
 * at image build time, based on a system property.
 *
 * Since logging has to execute before monitors are available, any synchronization
 * must be handled with compare and swap operations.
 *
 * This code can also execute in {@link MaxineVM#isHosted() hosted} mode, to support
 * logging/tracing during boot image generation. The {@link VMLogHosted} implementation
 * is used during hosted mode.
 */
public abstract class VMLog implements Heap.GCCallback {

    /**
     * Factory class to choose implementation at image build time via property.
     */
    public static class Factory {
        private static final String VMLOG_FACTORY_CLASS = "max.vmlog.class";
        public static final String DEFAULT_VMLOG_CLASS = "nat.thread.var.VMLogNativeThreadVariable";

        private static VMLog create() {
            String prop = System.getProperty(VMLOG_FACTORY_CLASS);
            String className = DEFAULT_VMLOG_CLASS;
            if (prop != null) {
                className = prop;
            }
            String name = VMLog.class.getPackage().getName() + "." + className;
            try {
                return (VMLog) Class.forName(name).newInstance();
            } catch (Exception ex) {
                ProgramError.unexpected("failed to create " + name, ex);
                return null;
            }
        }

        public static boolean is(String name) {
            String prop = System.getProperty(VMLOG_FACTORY_CLASS);
            return prop == null ? name.equals(DEFAULT_VMLOG_CLASS) : name.equals(prop);
        }
    }

    /**
     * The bare essentials of a log record. A log record contains:
     * <ul>
     * <li>The {@link VMLogger} id that created the record.
     * <li>The operation code.
     * <li>The thread that created the record.
     * <li>Up to {@value MAX_ARGS} {@link Word} valued arguments.
     * </ul>
     * Everything except the arguments is stored in a {@code header} word,
     * the format of which is specified here as a set of bit fields.
     * The actual representation of the record is left to concrete subclasses.
     * <p>
     * Note that, although, the {@link VMLog} maintains a monotonically increasing
     * {@link VMLog#nextId globally unique id}, that is incremented each time a record
     * is allocated, this value is not stored by default in the log record.
     * The log is typically viewed in the Maxine Inspector, which is capable of
     * reproducing the id in the log view.
     *
     */
    public abstract static class Record {
        public static final int ARGCOUNT_MASK = 0xF;
        public static final int LOGGER_ID_SHIFT = 4;
        public static final int LOGGER_ID_MASK = 0x1F;
        public static final int OPERATION_SHIFT = 9;
        public static final int OPERATION_MASK = 0xFF;
        public static final int THREAD_SHIFT = 17;
        public static final int THREAD_MASK = 0x3FFF;
        public static final int FREE = 0x80000000;
        public static final int MAX_ARGS = 8;

        public static int getOperation(int header) {
            return (header >> Record.OPERATION_SHIFT) & OPERATION_MASK;
        }

        public static int getLoggerId(int header) {
            return (header >> LOGGER_ID_SHIFT) & Record.LOGGER_ID_MASK;
        }

        public static int getThreadId(int header) {
            return (header >> THREAD_SHIFT) & THREAD_MASK;
        }

        public static boolean isFree(int header) {
            return (header & FREE) != 0;
        }

        public static int getArgCount(int header) {
            return header & Record.ARGCOUNT_MASK;
        }

        /**
         * Encodes the loggerId, the operation and the argument count.
         * Bits 0-3: argument count (max 15)
         * Bits 4-8: logger id (max 32)
         * Bits 9-16: operation id (max 256)
         * Bits 17-30: threadId (max 16384)
         * Bit 31: FREE (1) for implementations that have free lists
         */
        public abstract void setHeader(int header);
        public abstract int getHeader();

        public void setHeader(int op, int argCount, int loggerId) {
            setHeader((safeGetThreadId() << THREAD_SHIFT) | (op << Record.OPERATION_SHIFT) |
                      (loggerId << Record.LOGGER_ID_SHIFT) | argCount);
        }

        private static int safeGetThreadId() {
            if (MaxineVM.isHosted()) {
                return (int) Thread.currentThread().getId();
            } else {
                VmThread vmThread = VmThread.current();
                return vmThread == null ? 0 : vmThread.id();
            }
        }

        public void setFree() {
            setHeader(FREE);
        }

        public int getThreadId() {
            return getThreadId(getHeader());
        }
        public int getOperation() {
            return getOperation(getHeader());
        }
        public int getLoggerId() {
            return getLoggerId(getHeader());
        }
        public int getArgCount() {
            return getArgCount(getHeader());
        }

        /**
         * Return value of argument {@code n: 1 <= N}.
         * @param n
         * @return the argument at index {@code n}
         */
        public Word getArg(int n) {
            return argError();
        }

        public int getIntArg(int n) {
            return getArg(n).asAddress().toInt();
        }

        public long getLongArg(int n) {
            return getArg(n).asAddress().toLong();
        }

        public boolean getBooleanArg(int n) {
            return getArg(n).isNotZero();
        }

        public void setArgs(Word arg1) {
            argError();
        }
        public void setArgs(Word arg1, Word arg2) {
            argError();
        }
        public void setArgs(Word arg1, Word arg2, Word arg3) {
            argError();
        }
        public void setArgs(Word arg1, Word arg2, Word arg3, Word arg4) {
            argError();
        }
        public void setArgs(Word arg1, Word arg2, Word arg3, Word arg4, Word arg5) {
            argError();
        }
        public void setArgs(Word arg1, Word arg2, Word arg3, Word arg4, Word arg5, Word arg6) {
            argError();
        }
        public void setArgs(Word arg1, Word arg2, Word arg3, Word arg4, Word arg5, Word arg6, Word arg7) {
            argError();
        }
        public void setArgs(Word arg1, Word arg2, Word arg3, Word arg4, Word arg5, Word arg6, Word arg7, Word arg8) {
            argError();
        }
    }

    public static Word argError() {
        assert false;
        return Word.zero();
    }

    private static final String LOG_ENTRIES_PROPERTY = "max.vmlog.entries";
    private final static int DEFAULT_LOG_ENTRIES = 8192;

    /**
     * Offset to {@link #nextId} used in compare and swap.
     */
    @CONSTANT_WHEN_NOT_ZERO
    private static int nextIdOffset;

    /**
     * List used to accumulate loggers during registration phase.
     */
    @HOSTED_ONLY
    private static ArrayList<VMLogger> loggerList = new ArrayList<VMLogger>();

    /**
     * Array of registered {@link VMLogger} instances.
     */
    @INSPECTED
    @CONSTANT_WHEN_NOT_ZERO
    private static VMLogger[] loggers;

    /**
     * Number of log records maintained in the circular buffer.
     */
    @INSPECTED
    protected int logEntries;

    /**
     * The actual {@link VMLog} instance in this VM image.
     */
    @INSPECTED
    @CONSTANT_WHEN_NOT_ZERO
    private static VMLog vmLog;

    /**
     * Monotonically increasing global unique id for a log record.
     * Incremented every time {@link #getRecord(int)} is invoked.
     */
    @INSPECTED
    protected volatile int nextId;

    /**
     * Array of refMaps indexed by the logger id.
     * This array is indexed by {@link VMLogger#loggerId} which
     * ranges from {@code 1..loggers.length} for a valid logger.
     * So this array is one element larger than {@link #loggers}.
     */
    @CONSTANT_WHEN_NOT_ZERO
    protected static int[][] operationRefMaps;

    /**
     * Called to create the specific {@link VMLog} subclass at an appropriate point in the image build.
     */
    @HOSTED_ONLY
    static class InitializationCompleteCallback implements com.sun.max.vm.hosted.JavaPrototype.InitializationCompleteCallback {

        public void initializationComplete() {
            nextIdOffset = ClassActor.fromJava(VMLog.class).findLocalInstanceFieldActor("nextId").offset();
            vmLog = Factory.create();
            vmLog.initialize(MaxineVM.Phase.BOOTSTRAPPING);
            loggers = new VMLogger[loggerList.size()];
            loggerList.toArray(loggers);
            operationRefMaps = new int[loggers.length + 1][];
            for (VMLogger logger : loggers) {
                if (logger != null) {
                    logger.setVMLog(vmLog, new VMLogHosted());
                    operationRefMaps[logger.loggerId] = logger.operationRefMaps;
                }
            }
            Heap.registerGCCallback(vmLog);
        }
    }

    static {
        JavaPrototype.registerInitializationCompleteCallback(new InitializationCompleteCallback());
    }

    /**
     * Phase specific initialization.
     * Only called for BOOTSTRAPPING, PRIMORDIAL, TERMINATING.
     * @param phase the phase
     */
    public void initialize(MaxineVM.Phase phase) {
        // logging options will have been checked and set during BOOTSTRAPPING,
        // they need to be reset now for the actual VM run.
        if (phase == MaxineVM.Phase.PRIMORDIAL) {
            for (int i = 0; i < loggers.length; i++) {
                VMLogger logger = loggers[i];
                if (logger != null) {
                    logger.setDefaultState();
                }
            }
        }
    }

    /**
     * Called when a new thread is started so any thread-specific log state can be setup.
     */
    public void threadStart() {
    }

    public static VMLog vmLog() {
        return vmLog;
    }

    @HOSTED_ONLY
    public static void registerLogger(VMLogger logger) {
        loggerList.add(logger);
    }

    private void checkLogEntriesProperty() {
        String logSizeProperty = System.getProperty(LOG_ENTRIES_PROPERTY);
        if (logSizeProperty != null) {
            logEntries = Integer.parseInt(logSizeProperty);
        } else {
            logEntries = DEFAULT_LOG_ENTRIES;
        }
    }

    protected VMLog() {
        checkLogEntriesProperty();
    }


    /**
     * Called once the VM is up to check for limitations on logging.
     *
     */
    public static void checkLogOptions() {
        for (int i = 0; i < loggers.length; i++) {
            if (loggers[i] != null) {
                loggers[i].checkOptions();
            }
        }
    }

    /**
     * Allocate a monotonically increasing unique id for a log record.
     *
     * @return
     */
    @INLINE
    @NO_SAFEPOINT_POLLS("atomic")
    protected final int getUniqueId() {
        if (MaxineVM.isHosted()) {
            synchronized (this) {
                return nextId++;
            }
        } else {
            int myId = nextId;
            while (Reference.fromJava(this).compareAndSwapInt(nextIdOffset, myId, myId + 1) != myId) {
                myId = nextId;
            }
            return myId;
        }
    }

    /**
     * Acquire a record that is capable of storing at least {@code argCount} arguments.
     * N.B. This value should be considered single use and not cached.
     * @param argCount
     * @return
     */
    protected abstract Record getRecord(int argCount);

    /**
     * Controls logging (for all loggers) for the current thread.
     * Initially logging is enabled.
     *
     * @param state {@code true} to enable, {@code false} to disable.
     * @return state on entry
     */
    public abstract boolean setThreadState(boolean state);

    /**
     *
     * @return {@code true} if logging is enabled for the current thread.
     */
    public abstract boolean threadIsEnabled();

    /**
     * Scan the log for reference types for GC.
     * @param tla tla for thread
     * @param visitor
     */
    public abstract void scanLog(Pointer tla, PointerIndexVisitor visitor);

    /**
     * Records the identify of the last visitor passed to {@link #scanLog} to avoid repeat scans
     * of global log buffers.
     * N.B. this assumes no parallel calls on multiple threads.
     */
    private PointerIndexVisitor lastVisitor;

    @Override
    public void gcCallback(GCCallbackPhase gcCallbackPhase) {
        if (gcCallbackPhase == GCCallbackPhase.AFTER) {
            lastVisitor = null;
        }
    }

    /**
     * Returns {@code true} is {@code visitor} is the same as the last call.
     * @param visitor
     * @return
     */
    protected boolean isRepeatScanLogVisitor(PointerIndexVisitor visitor) {
        // if it's the same visitor (and not null) it's a repeat call for a different thread.
        if (lastVisitor == visitor) {
            return true;
        } else {
            lastVisitor = visitor;
            return false;
        }
    }

    /**
     * Encapsulates the logic of visiting reference valued arguments.
     * @param r the log record
     * @param argBase the address of the base of the arguments
     * @param visitor the visitor originally passed to {@link #scanLog}.
     */
    protected void scanArgs(Record r, Pointer argBase, PointerIndexVisitor visitor) {
        int loggerId = r.getLoggerId();
        int[] loggerOperationRefMaps = operationRefMaps[loggerId];
        if (loggerOperationRefMaps != null) {
            int op = r.getOperation();
            int operationRefMap = loggerOperationRefMaps[op];
            int argIndex = 0;
            while (operationRefMap != 0) {
                if ((operationRefMap & 1) != 0) {
                    visitor.visit(argBase, argIndex);
                }
                argIndex++;
                operationRefMap = operationRefMap >>> 1;
            }
        }
    }

}
