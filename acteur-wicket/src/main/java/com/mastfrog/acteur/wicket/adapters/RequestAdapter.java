/*
 * The MIT License
 *
 * Copyright 2015 Tim Boudreau.
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

import com.mastfrog.acteur.HttpEvent;
import com.mastfrog.acteur.headers.Headers;
import com.mastfrog.acteur.server.PathFactory;
import com.mastfrog.acteur.server.ServerModule;
import com.mastfrog.settings.Settings;
import com.mastfrog.util.collections.CollectionUtils;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import javax.inject.Inject;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.util.string.Strings;
import org.apache.wicket.util.time.Time;
import org.joda.time.DateTime;

/**
 *
 * @author Tim Boudreau
 */
public class RequestAdapter extends WebRequest {

    public final HttpEvent evt;
    private final Locale locale;
    private final Charset charset;
    private final Url url;

    @Inject
    public RequestAdapter(HttpEvent evt, Locale locale, Charset charset, Settings settings) {
        this.evt = evt;
        this.locale = locale;
        this.charset = charset;
        String filterPrefix = settings.getString(PathFactory.BASE_PATH_SETTINGS_KEY, "");
        String uri = evt.getRequest().getUri();
        // Adapted from ServletWebRequest's constructor
        if (filterPrefix.length() > 0 && !filterPrefix.endsWith("/")) {
            filterPrefix += "/";
        }
        StringBuilder sb = new StringBuilder();
        uri = Strings.stripJSessionId(uri);
        String contextPath = "";
        final int start = contextPath.length() + filterPrefix.length() + 1;
        if (uri.length() > start) {
            sb.append(uri.substring(start));
        }

        Url url = Url.parse(sb.toString(), charset, false);
        url.setPort(settings.getInt(ServerModule.PORT));
        String host = evt.getHeader(Headers.HOST);
        if (host != null) {
            url.setHost(host);
        } else {
            url.setHost(settings.getString(PathFactory.HOSTNAME_SETTINGS_KEY, "localhost"));
        }
        url.setProtocol("http");
        this.url = url;
    }

    @Override
    public Url getUrl() {
        return url;
    }

    @Override
    public Url getClientUrl() {
        return getUrl();
    }

    @Override
    public Locale getLocale() {
        return locale;
    }

    @Override
    public Charset getCharset() {
        return charset;
    }

    @Override
    public Object getContainerRequest() {
        return evt;
    }

    @Override
    public List<javax.servlet.http.Cookie> getCookies() {
        io.netty.handler.codec.http.Cookie[] cookies = evt.getHeader(Headers.COOKIE);
        List<io.netty.handler.codec.http.Cookie> cks = Arrays.asList(cookies);
        return CollectionUtils.convertedList(cks, new CookieConverter(), io.netty.handler.codec.http.Cookie.class, javax.servlet.http.Cookie.class);
    }

    @Override
    public List<String> getHeaders(String string) {
        return CollectionUtils.oneItemList(getHeader(string));
    }

    @Override
    public String getHeader(String string) {
        return evt.getHeader(Headers.stringHeader(string));
    }

    @Override
    public Time getDateHeader(String string) {
        String hdr = getHeader(string);
        if (hdr == null) {
            return null;
        }
        DateTime dt = Headers.ISO2822DateFormat.parseDateTime(hdr);
        return Time.valueOf(dt.toDate());
    }
}
