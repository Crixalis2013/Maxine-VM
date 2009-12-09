/*
 * Copyright (c) 2009 Sun Microsystems, Inc.  All rights reserved.
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
package jtt.optimize;

/*
 * Tests value numbering of integer operations.
 * @Harness: java
 * @Runs: 0=6; 1=8; 2=10; 3=12; 4=0
 */
public class VN_Loop01 {
    private static boolean cond1 = true;
    private static boolean cond2 = true;

    public static int test(int arg) {
        if (arg == 0) {
            return test1(arg);
        }
        if (arg == 1) {
            return test2(arg);
        }
        if (arg == 2) {
            return test3(arg);
        }
        if (arg == 3) {
            return test4(arg);
        }
        return 0;
    }
    public static int test1(int x) {
        int c = 3;
        int t = x + c;
        while (cond1) {
            if (cond2) {
                int u = x + c; // GVN should recognize u == t
                return t + u;
            }
        }
        return 3; // GVN should recognize 3 == 3
    }
    public static int test2(int x) {
        int c = 3;
        while (cond1) {
            int t = x + c;
            if (cond2) {
                int u = x + c; // GVN should recognize u == t
                return t + u;
            }
        }
        return 3;
    }
    public static int test3(int x) {
        int c = 3;
        int t = x + c;
        while (cond1) {
            if (cond2) {
                int u = x + c; // GVN should recognize u == t
                return t + u;
            }
            int u = x + c; // GVN should recognize u == t
            return t + u;
        }
        return 3; // GVN should recognize 3 == 3
    }
    public static int test4(int x) {
        int c = 3;
        int t = x + c;
        while (cond1) {
            if (!cond2) {
                int u = x + c;
                return t + u;
            }
            int u = x + c;
            return t + u;
        }
        return 3;
    }
}
