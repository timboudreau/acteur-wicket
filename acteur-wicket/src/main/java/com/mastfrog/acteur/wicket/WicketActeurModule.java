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
package com.mastfrog.acteur.wicket;

import com.mastfrog.acteur.wicket.emulation.FakeWicketFilter;
import com.mastfrog.acteur.wicket.emulation.FakeServletContext;
import com.mastfrog.acteur.wicket.emulation.FakeFilterConfig;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.mastfrog.acteur.wicket.page.WicketGuiceModule;
import com.mastfrog.guicy.scope.ReentrantScope;
import java.util.Locale;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import org.apache.wicket.Application;
import org.apache.wicket.protocol.http.WicketFilter;
import org.apache.wicket.session.ISessionStore;

/**
 *
 * @author Tim Boudreau
 */
public class WicketActeurModule extends AbstractModule {

    private final WicketConfig config;
    private final ReentrantScope scope;

    public WicketActeurModule(WicketConfig config, ReentrantScope scope) {
        this.config = config;
        this.scope = scope;
    }

    public WicketActeurModule(Class<? extends Application> type, ReentrantScope scope) {
        this(new DefaultConfig(type), scope);
    }

    @Override
    protected void configure() {
        bind(Application.class).toProvider(WicketApplicationProvider.class);
        bind(WicketConfig.class).toInstance(config);
        install(new WicketGuiceModule(scope));
        bind(ServletContext.class).to(FakeServletContext.class).in(Scopes.SINGLETON);
        bind(WicketFilter.class).to(FakeWicketFilter.class).in(Scopes.SINGLETON);
        bind(FilterConfig.class).to(FakeFilterConfig.class).in(Scopes.SINGLETON);
        bind(ISessionStore.class).to(ActeurSessionStore.class).in(Scopes.SINGLETON);
    }

    private static class DefaultConfig implements WicketConfig {

        private final Class<? extends Application> type;

        public DefaultConfig(Class<? extends Application> type) {
            this.type = type;
        }

        @Override
        public Class<? extends Application> applicationClass() {
            return type;
        }

        @Override
        public Locale locale() {
            return Locale.getDefault();
        }
    }
}
