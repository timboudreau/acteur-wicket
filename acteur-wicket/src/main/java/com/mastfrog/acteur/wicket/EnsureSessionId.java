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

import com.mastfrog.acteur.Acteur;
import com.mastfrog.acteur.HttpEvent;
import com.mastfrog.acteur.headers.Headers;
import static com.mastfrog.acteur.wicket.WicketActeurModule.DEFAULT_SESSION_COOKIE_MAX_AGE_HOURS;
import static com.mastfrog.acteur.wicket.WicketActeurModule.SETTINGS_KEY_SESSION_COOKIE_MAX_AGE_HOURS;
import com.mastfrog.settings.Settings;
import io.netty.handler.codec.http.Cookie;
import io.netty.handler.codec.http.DefaultCookie;
import javax.inject.Inject;
import org.apache.wicket.Application;
import org.apache.wicket.protocol.http.IRequestLogger;
import org.joda.time.Duration;

/**
 * Ensures that a jsessionid cookie is set and the current session id is
 * available for injection.
 *
 * @author Tim Boudreau
 */
final class EnsureSessionId extends Acteur {

    public static final Duration MAX_SESSION_AGE = Duration.standardDays(1);

    @Inject
    EnsureSessionId(HttpEvent evt, Application app, Settings settings) {
        SessionId id = findSessionId(evt);
        if (id == null) {
            id = new SessionId();
            DefaultCookie ck = new DefaultCookie(ActeurSessionStore.COOKIE_NAME, id.toString());
            long maxAge = Duration.standardHours(
                    settings.getLong(SETTINGS_KEY_SESSION_COOKIE_MAX_AGE_HOURS, DEFAULT_SESSION_COOKIE_MAX_AGE_HOURS)).toStandardSeconds().getSeconds();
            ck.setMaxAge(maxAge);
            add(Headers.SET_COOKIE, ck);
            String sv = Headers.COOKIE.toString(new Cookie[]{ck});
            evt.getRequest().headers().add(Headers.SET_COOKIE.name(), sv);
            IRequestLogger logger = app.getRequestLogger();
            if (logger != null) {
                logger.sessionCreated(id.toString());
            }
        }
        setState(new ConsumedLockedState(id));
    }

    /**
     * Locate the session id in the request cookies
     * @param evt The HTTP request
     * @return A session id if the cookie is present, or null if it is not
     */
    static SessionId findSessionId(HttpEvent evt) {
        Cookie[] cookies = evt.getHeader(Headers.COOKIE);
        SessionId result = null;
        if (cookies != null && cookies.length > 0) {
            for (Cookie ck : cookies) {
                if (ActeurSessionStore.COOKIE_NAME.equals(ck.getName())) {
                    result = new SessionId(ck.getValue());
                }
            }
        }
        return result;
    }
}
