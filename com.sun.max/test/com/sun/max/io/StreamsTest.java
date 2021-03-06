/*
 * Copyright (c) 2007, 2011, Oracle and/or its affiliates. All rights reserved.
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
package com.sun.max.io;

import java.io.*;

import junit.framework.*;

public class StreamsTest extends TestCase {

    public StreamsTest(String name) {
        super(name);
    }

    private static BufferedInputStream asStream(String s) {
        return new BufferedInputStream(new ByteArrayInputStream(s.getBytes()));
    }

    private static boolean streamSearch(String content, String... keys) {
        try {
            final BufferedInputStream stream = asStream(content);
            for (String key : keys) {
                if (!Streams.search(stream, key.getBytes())) {
                    return false;
                }
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void test_search() {
        assertTrue(streamSearch("search", "search"));
        assertTrue(streamSearch("search", ""));
        assertTrue(streamSearch("", ""));
        assertTrue(streamSearch("search", "sea"));
        assertTrue(streamSearch("seasearch", "search"));
        assertTrue(streamSearch("one two three", "one", "two", "three"));

        assertFalse(streamSearch("se arch", "sea"));
        assertFalse(streamSearch("", "key"));
    }
}
