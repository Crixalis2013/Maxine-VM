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
package com.sun.max.vm.cps.dir;

import com.sun.max.vm.type.*;
import com.sun.max.vm.value.*;

/**
 * A value that is constant at compile time.
 *
 * @author Bernd Mathiske
 */
public class DirConstant extends DirValue {

    private final Value value;

    public DirConstant(Value value) {
        this.value = value;
    }

    public Kind kind() {
        return value.kind();
    }

    @Override
    public Value value() {
        return value;
    }

    public boolean isConstant() {
        return true;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof DirConstant) {
            final DirConstant dirConstant = (DirConstant) other;
            return value.equals(dirConstant.value);
        }
        return false;
    }

    @Override
    public int hashCodeForBlock() {
        if (value.kind().isReference) {
            return super.hashCodeForBlock();
        }
        return super.hashCodeForBlock() ^ value.hashCode();
    }

    @Override
    public String toString() {
        return value.toString();
    }

    public static final DirConstant VOID = new DirConstant(VoidValue.VOID);
}