/*
 * Copyright (c) 2017-2019, APT Group, School of Computer Science,
 * The University of Manchester. All rights reserved.
 * Copyright (c) 2009, 2011, Oracle and/or its affiliates. All rights reserved.
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
 */
package com.sun.c1x.ir;

import com.sun.c1x.debug.*;

/**
 * The {@link ValueVisitor} implements one half of the visitor
 * pattern for {@linkplain Value IR values}, allowing clients to implement functionality
 * depending on the type of an value without doing type tests.
 */
public abstract class ValueVisitor {
    // Checkstyle: off
    public abstract void visitDebugMethodID(DebugMethodID i);
    public abstract void visitArithmeticOp(ArithmeticOp i);
    public abstract void visitArrayCopy(ArrayCopy arrayCopy);
    public abstract void visitArrayLength(ArrayLength i);
    public abstract void visitBase(Base i);
    public abstract void visitBoundsCheck(BoundsCheck boundsCheck);
    public abstract void visitBlockBegin(BlockBegin i);
    public abstract void visitBreakpointTrap(BreakpointTrap i);
    public abstract void visitCheckCast(CheckCast i);
    public abstract void visitCompareOp(CompareOp i);
    public abstract void visitCompareAndSwap(CompareAndSwap i);
    public abstract void visitConstant(Constant i);
    public abstract void visitConvert(Convert i);
    public abstract void visitExceptionObject(ExceptionObject i);
    public abstract void visitGoto(Goto i);
    public abstract void visitIf(If i);
    public abstract void visitIfOp(IfOp i);
    public abstract void visitInstanceOf(InstanceOf i);
    public abstract void visitIntrinsic(Intrinsic i);
    public abstract void visitInvoke(Invoke i);
    public abstract void visitInvokeHandle(InvokeHandle invokeHandle);
    public abstract void visitLoadField(LoadField i);
    public abstract void visitLoadIndexed(LoadIndexed i);
    public abstract void visitInfopoint(Infopoint i);
    public abstract void visitLinkTo(LinkTo linkTo);
    public abstract void visitLoadPointer(LoadPointer i);
    public abstract void visitLoadRegister(LoadRegister i);
    public abstract void visitLocal(Local i);
    public abstract void visitLogicOp(LogicOp i);
    public abstract void visitLookupSwitch(LookupSwitch i);
    public abstract void visitMemoryBarrier(MemoryBarrier memoryBarrier);
    public abstract void visitMonitorAddress(MonitorAddress monitorAddress);
    public abstract void visitMonitorEnter(MonitorEnter i);
    public abstract void visitMonitorExit(MonitorExit i);
    public abstract void visitNativeCall(NativeCall i);
    public abstract void visitNegateOp(NegateOp i);
    public abstract void visitNewInstance(NewInstance i);
    public abstract void visitNewMultiArray(NewMultiArray i);
    public abstract void visitNewObjectArray(NewObjectArray i);
    public abstract void visitNewObjectArrayClone(NewObjectArrayClone newObjectArrayClone);
    public abstract void visitNewTypeArray(NewTypeArray i);
    public abstract void visitNullCheck(NullCheck i);
    public abstract void visitOsrEntry(OsrEntry i);
    public abstract void visitPause(Pause i);
    public abstract void visitPhi(Phi i);
    public abstract void visitResolveClass(ResolveClass i);
    public abstract void visitReturn(Return i);
    public abstract void visitShiftOp(ShiftOp i);
    public abstract void visitSignificantBit(SignificantBitOp i);
    public abstract void visitAlloca(Alloca i);
    public abstract void visitStoreField(StoreField i);
    public abstract void visitStoreIndexed(StoreIndexed i);
    public abstract void visitStorePointer(StorePointer i);
    public abstract void visitStoreRegister(StoreRegister i);
    public abstract void visitTableSwitch(TableSwitch i);
    public abstract void visitThrow(Throw i);
    public abstract void visitTypeEqualityCheck(TypeEqualityCheck typeEqualityCheck);
    public abstract void visitUnsafeCast(UnsafeCast i);
    public abstract void visitUnsafeGetObject(UnsafeGetObject i);
    public abstract void visitUnsafeGetRaw(UnsafeGetRaw i);
    public abstract void visitUnsafePrefetchRead(UnsafePrefetchRead i);
    public abstract void visitUnsafePrefetchWrite(UnsafePrefetchWrite i);
    public abstract void visitUnsafePutObject(UnsafePutObject i);
    public abstract void visitUnsafePutRaw(UnsafePutRaw i);
    public abstract void visitUnsignedCompareOp(UnsignedCompareOp i);
    public abstract void visitIfBit(IfBit i);
    public abstract void visitGetTicks(GetTicks i);
    public abstract void visitGetCpuID(GetCpuID i);
}
