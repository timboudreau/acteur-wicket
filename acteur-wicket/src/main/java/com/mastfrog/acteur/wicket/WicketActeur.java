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
package com.mastfrog.acteur.wicket;

import com.mastfrog.acteur.Acteur;
import com.mastfrog.acteur.HttpEvent;
import com.mastfrog.acteur.annotations.HttpCall;
import com.mastfrog.acteur.annotations.Precursors;
import static com.mastfrog.acteur.headers.Method.DELETE;
import static com.mastfrog.acteur.headers.Method.GET;
import static com.mastfrog.acteur.headers.Method.HEAD;
import static com.mastfrog.acteur.headers.Method.POST;
import static com.mastfrog.acteur.headers.Method.PUT;
import com.mastfrog.acteur.preconditions.Methods;
import com.mastfrog.acteur.server.PathFactory;
import com.mastfrog.acteur.wicket.adapters.RequestAdapter;
import com.mastfrog.acteur.wicket.adapters.ResponseAdapter;
import com.mastfrog.guicy.scope.ReentrantScope;
import com.mastfrog.settings.Settings;
import com.mastfrog.util.thread.QuietAutoCloseable;
import io.netty.buffer.ByteBufAllocator;
import io.netty.handler.codec.http.HttpResponseStatus;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import java.nio.charset.Charset;
import javax.inject.Inject;
import org.apache.wicket.Application;
import org.apache.wicket.ThreadContext;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Response;
import org.apache.wicket.request.cycle.RequestCycle;

/**
 *
 * @author Tim Boudreau
 */
@HttpCall(order = Integer.MAX_VALUE, scopeTypes = {SessionId.class, Request.class, Response.class, com.mastfrog.acteur.Response.class})
@Methods({GET, PUT, POST, DELETE, HEAD})
@Precursors(EnsureSessionId.class)
public class WicketActeur extends Acteur {

    @Inject
    WicketActeur(HttpEvent evt, Application application, PathFactory pf, Charset charset, WicketConfig config, ByteBufAllocator alloc, Settings settings, ReentrantScope scope) {
        RequestAdapter request = new RequestAdapter(evt, config.locale(), charset, settings);
        ResponseAdapter response = new ResponseAdapter(response(), charset, alloc, pf);
        try (QuietAutoCloseable closeScope = scope.enter(request, response, response())) {
            ThreadContext.setApplication(application);
            RequestCycle requestCycle = application.createRequestCycle(request, response);
            ThreadContext.setRequestCycle(requestCycle);
            boolean processed = requestCycle.processRequestAndDetach();
            if (!processed) {
                reject();
            } else {
                HttpResponseStatus status = response.status();
                reply(status == null ? OK : status);
            }
        } finally {
            ThreadContext.detach();
        }
    }
}
