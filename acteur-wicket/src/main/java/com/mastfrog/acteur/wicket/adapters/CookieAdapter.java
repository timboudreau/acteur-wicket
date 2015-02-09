/*
 * The MIT License
 *
 * Copyright 2015 Tim Boudreau
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

/**
 *
 * @author Tim Boudreau
 */
class CookieAdapter extends javax.servlet.http.Cookie {

    final io.netty.handler.codec.http.Cookie cookie;

    public CookieAdapter(io.netty.handler.codec.http.Cookie cookie) {
        super(cookie.getName(), cookie.getValue());
        this.cookie = cookie;
    }

    @Override
    public void setVersion(int v) {
        cookie.setVersion(v);
    }

    @Override
    public int getVersion() {
        return cookie.getVersion();
    }

    @Override
    public String getValue() {
        return cookie.getValue();
    }

    @Override
    public void setValue(String newValue) {
        cookie.setValue(newValue);
    }

    @Override
    public String getName() {
        return cookie.getName();
    }

    @Override
    public boolean getSecure() {
        return cookie.isSecure();
    }

    @Override
    public void setSecure(boolean flag) {
        cookie.setSecure(flag);
    }

    @Override
    public String getPath() {
        return cookie.getPath();
    }

    @Override
    public void setPath(String uri) {
        cookie.setPath(uri);
    }

    @Override
    public int getMaxAge() {
        return (int) cookie.getMaxAge();
    }

    @Override
    public void setMaxAge(int expiry) {
        cookie.setMaxAge(expiry);
    }

    @Override
    public String getDomain() {
        return cookie.getDomain();
    }

    @Override
    public void setDomain(String pattern) {
        cookie.setDomain(pattern);
    }

    @Override
    public String getComment() {
        return cookie.getComment();
    }

    @Override
    public void setComment(String purpose) {
        cookie.setComment(purpose);
    }
}
