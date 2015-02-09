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

import com.google.common.net.MediaType;
import com.mastfrog.acteur.headers.Headers;
import com.mastfrog.acteur.server.PathFactory;
import com.mastfrog.url.Path;
import com.mastfrog.util.Exceptions;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.handler.codec.http.DefaultCookie;
import io.netty.handler.codec.http.DefaultHttpContent;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.LastHttpContent;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.inject.Inject;
import javax.servlet.http.Cookie;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.util.time.Time;
import org.joda.time.DateTime;

/**
 * Wraps an Acteur Response object and exposes it as a Wicket WebResponse
 * object.
 *
 * @author Tim Boudreau
 */
public class ResponseAdapter extends WebResponse {

    private final com.mastfrog.acteur.Response resp;
    private final Charset charset;
    private CFL cfl;
    private final ByteBufAllocator alloc;
    private HttpResponseStatus status;
    private final PathFactory paths;
    private boolean redir;
    boolean flushed;

    @Inject
    public ResponseAdapter(com.mastfrog.acteur.Response resp, Charset charset, ByteBufAllocator alloc, PathFactory paths) {
        this.resp = resp;
        this.charset = charset;
        this.alloc = alloc;
        this.paths = paths;
    }
    
    public HttpResponseStatus status() {
        return status;
    }

    @Override
    public void write(CharSequence cs) {
        write(cs.toString().getBytes(charset));
    }

    @Override
    public void write(byte[] bytes) {
        CFL cfl = cfl();
        cfl.add(bytes);
    }

    @Override
    public void write(byte[] bytes, int offset, int length) {
        byte[] copy = new byte[length];
        System.arraycopy(bytes, offset, copy, 0, length);
        write(copy);
    }

    @Override
    public String encodeURL(CharSequence cs) {
        return paths.constructURL(Path.parse(cs.toString()), false).toString();
    }

    @Override
    public Object getContainerResponse() {
        return resp;
    }

    private CFL cfl() {
        if (cfl == null) {
            cfl = new CFL(alloc);
            resp.setBodyWriter(cfl);
        }
        return cfl;
    }

    @Override
    public void addCookie(Cookie cookie) {
        resp.add(Headers.SET_COOKIE, CookieConverter.INSTANCE.unconvert(cookie));
    }

    @Override
    public void clearCookie(Cookie cookie) {
        DefaultCookie ck = (DefaultCookie) CookieConverter.INSTANCE.unconvert(cookie);
        ck.setDiscard(true);
        resp.add(Headers.SET_COOKIE, ck);
    }

    @Override
    public void setHeader(String string, String string1) {
        resp.add(Headers.stringHeader(string), string1);
    }

    @Override
    public void addHeader(String string, String string1) {
        resp.add(Headers.stringHeader(string), string1);
    }

    @Override
    public void setDateHeader(String string, Time time) {
        DateTime dt = new DateTime(time.getMilliseconds());
        String value = Headers.ISO2822DateFormat.print(dt);
        addHeader(string, value);
    }

    @Override
    public void setContentLength(long l) {
        resp.add(Headers.CONTENT_LENGTH, l);
    }

    @Override
    public void setContentType(String string) {
        resp.add(Headers.CONTENT_TYPE, MediaType.parse(string));
    }

    @Override
    public void setStatus(int i) {
        this.status = HttpResponseStatus.valueOf(i);
    }

    @Override
    public void sendError(int i, String string) {
        this.status = HttpResponseStatus.valueOf(i);
        resp.setMessage(string);
    }

    @Override
    public String encodeRedirectURL(CharSequence cs) {
        String s = cs.toString();
        if (s.startsWith("http:") || s.startsWith("https:")) {
            return s;
        }
        return paths.constructURL(Path.parse(s), false).toString(); // XXX https
    }

    @Override
    public void sendRedirect(String string) {
        setStatus(307);
        try {
            resp.add(Headers.LOCATION, new URI(encodeRedirectURL(string)));
        } catch (URISyntaxException ex) {
            Exceptions.chuck(ex);
        }
        redir = true;
    }

    @Override
    public boolean isRedirect() {
        return redir;
    }

    @Override
    public void flush() {
        //do nothing
        flushed = true;
    }

    static class CFL implements ChannelFutureListener {

        private final List<byte[]> bytes = new LinkedList<>();
        private Iterator<byte[]> iter;
        private final ByteBufAllocator alloc;

        public CFL(ByteBufAllocator alloc) {
            this.alloc = alloc;
        }

        CFL add(byte[] bytes) {
            if (iter != null) {
                throw new IllegalStateException("Cannot add bytes to write while content is being written");
            }
            if (bytes.length > 0) {
                this.bytes.add(bytes);
            }
            return this;
        }

        @Override
        public void operationComplete(ChannelFuture f) throws Exception {
            if (iter == null) {
                iter = bytes.iterator();
            }
            if (!iter.hasNext()) {
                f = f.channel().writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT).addListener(CLOSE);
                bytes.clear();
                iter = null;
            } else {
                byte[] bytes = iter.next();
                ByteBuf buf = alloc.buffer().writeBytes(bytes);
                f = f.channel().writeAndFlush(new DefaultHttpContent(buf)).addListener(this);
            }
        }
    }
}
