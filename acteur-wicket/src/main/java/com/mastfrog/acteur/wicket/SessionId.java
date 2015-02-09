/*
 * The MIT License
 *
 * Copyright 2015 tim.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.mastfrog.acteur.wicket;

import com.mastfrog.util.GUIDFactory;
import java.io.Serializable;
import java.util.concurrent.atomic.AtomicLong;

/**
 *
 * @author Tim Boudreau
 */
public class SessionId implements Serializable {

    private static final AtomicLong ids = new AtomicLong(Long.MIN_VALUE);
    private final String id;

    public SessionId(String id) {
        this.id = id;
    }

    public SessionId() {
        String rnd = GUIDFactory.get().newGUID(2, 5);
        String inc = Long.toString(ids.getAndIncrement(), 36);
        String dt = Long.toString(System.currentTimeMillis(), 36);
        this.id = rnd + ':' + inc + ':' + dt;
    }

    public boolean equals(Object o) {
        return o == this ? true : o instanceof SessionId && o.toString().equals(toString());
    }

    public int hashCode() {
        return id.hashCode();
    }

    public String toString() {
        return id;
    }
}
