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

package com.mastfrog.acteur.wicket.adapters;

import com.mastfrog.util.collections.Converter;
import io.netty.handler.codec.http.Cookie;
import io.netty.handler.codec.http.DefaultCookie;

/**
 * Interconverts between Servlet and Netty cookies.
 *
 * @author Tim Boudreau
 */
class CookieConverter implements Converter<javax.servlet.http.Cookie, io.netty.handler.codec.http.Cookie> {
    
    public static final CookieConverter INSTANCE = new CookieConverter();

    @Override
    public javax.servlet.http.Cookie convert(Cookie r) {
        return new CookieAdapter(r);
    }

    @Override
    public Cookie unconvert(javax.servlet.http.Cookie t) {
        if (t instanceof CookieAdapter) {
            return ((CookieAdapter) t).cookie;
        } else {
            DefaultCookie dc = new DefaultCookie(t.getName(), t.getValue());
            dc.setComment(t.getComment());
            dc.setMaxAge(t.getMaxAge());
            dc.setDomain(t.getDomain());
            dc.setVersion(t.getVersion());
            return dc;
        }
    }
}
