/*
 * Copyright (c) 2007 Sun Microsystems, Inc.  All rights reserved.
 *
 * Sun Microsystems, Inc. has intellectual property rights relating to technology embodied in the product
 * that is described in this document. In particular, and without limitation, these intellectual property
 * rights may include one or more of the U.S. patents listed at http://www.sun.com/patents and one or
 * more additional patents or pending patent applications in the U.S. and in other countries.
 *
 * U.S. Government Rights - Commercial software. Government users are subject to the Sun
 * Microsystems, Inc. standard license agreement and applicable provisions of the FAR and its
 * supplements.
 *
 * Use is subject to license terms. Sun, Sun Microsystems, the Sun logo, Java and Solaris are trademarks or
 * registered trademarks of Sun Microsystems, Inc. in the U.S. and other countries. All SPARC trademarks
 * are used under license and are trademarks or registered trademarks of SPARC International, Inc. in the
 * U.S. and other countries.
 *
 * UNIX is a registered trademark in the U.S. and other countries, exclusively licensed through X/Open
 * Company, Ltd.
 */
package com.sun.max.vm.compiler;

import com.sun.max.*;
import com.sun.max.unsafe.*;
import com.sun.max.vm.compiler.builtin.*;
import com.sun.max.vm.compiler.ir.*;
import com.sun.max.vm.stack.*;

/**
 * The compiler interface describes the extended interface compiler interface, which
 * includes more operations than a basic dynamic compiler.
 *
 * @author Bernd Mathiske
 * @author Doug Simon
 * @author Ben L. Titzer
 */
public interface CompilerScheme extends DynamicCompilerScheme {

    /**
     * Starts up the compiler by building built-in operations.
     *
     * @param packageLoader a package loader which can be used to load classes that define built-ins
     */
    void createBuiltins(PackageLoader packageLoader);

    /**
     * Starts up the compiler by building and optimizing snippets (i.e. internal pieces of IR
     * needed to translate from bytecodes to the compilers internal IR).
     *
     * @param packageLoader a package loader which can be used to load classes that define snippets
     */
    void createSnippets(PackageLoader packageLoader);

    /**
     * Optimize the internal snippets.
     */
    void compileSnippets();

    /**
     * Checks whether the compiler has finished compiling its internal snippets.
     *
     * @return {@code true} if the snippets are compiled; {@code false} otherwise
     */
    boolean areSnippetsCompiled();

    /**
     * Checks whether this compiler implements the specified built-in directly, or whether
     * a native method is required to implement the operation.
     *
     * @param builtin the builtin to check
     * @return {@code true} if this compiler supports the specified built-in; {@code false} otherwise
     */
    boolean isBuiltinImplemented(Builtin builtin);

    /**
     * The compiler's static trampoline. Calls to unresolved static methods will be linked to call
     * this method in order to resolve and/or compile their targets.
     */
    void staticTrampoline();

    /**
     * Gets this compiler's last IR generator (typically a {@link com.sun.max.vm.compiler.target.TargetGenerator}).
     *
     * @return the last IR generator of this compiler
     */
    IrGenerator irGenerator();

    /**
     * Selects and returns the address used as the base for
     * {@linkplain MakeStackVariable.StackVariable named stack variables}.
     *
     * @param stackPointer the stack pointer value for a frame being {@linkplain StackFrameWalker walked} that has a
     *            named stack variable
     * @param framePointer the frame pointer value for a frame being {@linkplain StackFrameWalker walked} that has a
     *            named stack variable
     * @return either {@code stackPointer} or {@code framePointer}, depending on which value is used as the base for
     *         accessing a name stack variable in the denoted frame
     */
    Pointer namedVariablesBasePointer(Pointer stackPointer, Pointer framePointer);

    /**
     * Make the current stub frame (which is incomplete, because it belongs to a trap stub)
     * look like a complete call frame with the given return address.
     *
     * @param returnAddress the return address for the ficticious call of the stub
     */
    void fakeCall(Address returnAddress);
}
