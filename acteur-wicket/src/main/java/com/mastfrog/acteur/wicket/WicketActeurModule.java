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

import com.google.inject.AbstractModule;
import com.google.inject.Provider;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import com.mastfrog.guicy.scope.ReentrantScope;
import java.util.Locale;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import org.apache.wicket.Application;
import org.apache.wicket.IPageFactory;
import org.apache.wicket.protocol.http.WicketFilter;
import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.session.ISessionStore;

/**
 * Entry point for starting a Wicket application in an Acteur server.
 * The easy way to use this is to subclass it, leave your subclass with a
 * constructor that takes a ReentrantScope, and pass that <i>type</i> to
 * SettingsBuilder.add().
 *
 * @author Tim Boudreau
 */
public class WicketActeurModule extends AbstractModule {

    private final WicketConfig config;
    private final ReentrantScope scope;
    /**
     * Value in hours of the max age a session cookie should have.  The
     * default is 48.
     */
    public static final String SETTINGS_KEY_SESSION_COOKIE_MAX_AGE_HOURS = "session.duration.hours";
    /** The default value for session duration in hours, if not set in settings */
    public static final long DEFAULT_SESSION_COOKIE_MAX_AGE_HOURS = 48;

    /**
     * Create a Wicket Acteur Module with an explicitly defined config
     * @param config The application configuration
     * @param scope The scope, which is used for injectable stuff by Guice - see
     * SettingsBuilder.
     */
    public WicketActeurModule(WicketConfig config, ReentrantScope scope) {
        this.config = config;
        this.scope = scope;
    }

    /**
     * Create a Wicket Acteur Module using the passed type.
     * 
     * @param type The applicatioon type
     * @param scope The scope
     */
    public WicketActeurModule(Class<? extends Application> type, ReentrantScope scope) {
        this(new DefaultConfig(type), scope);
    }

    @Override
    protected void configure() {
        bind(Application.class).toProvider(WicketApplicationProvider.class);
        bind(WicketConfig.class).toInstance(config);
        bind(ServletContext.class).to(FakeServletContext.class).in(Scopes.SINGLETON);
        bind(WicketFilter.class).to(FakeWicketFilter.class).in(Scopes.SINGLETON);
        bind(FilterConfig.class).to(FakeFilterConfig.class).in(Scopes.SINGLETON);
        bind(ISessionStore.class).to(ActeurSessionStore.class).in(Scopes.SINGLETON);
        // Make sure a PageParameters is always available, for instantiating
        // pages - will be overrlaid with the page parameters in created from
        // the URL by the GuicePageFactory if there are real parameters to use
        binder().bind(PageParameters.class).toProvider(
                scope.provider(PageParameters.class, new EmptyPageParametersProvider()));
        // Our custom page factory that will use Guice to create page instances
        // It is injected into the Application instance
        binder().bind(IPageFactory.class).to(GuicePageFactory.class);
        // Request ids give us a way to trace a request across database calls,
        // etc..
        binder().bind(IRequestParameters.class).toProvider(RequestParametersProvider.class);
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
    

    private static final class EmptyPageParametersProvider implements Provider<PageParameters> {

        @Override
        public PageParameters get() {
            return new PageParameters();
        }
    }

    @Singleton
    private static final class RequestParametersProvider implements Provider<IRequestParameters> {

        @Override
        public IRequestParameters get() {
            return RequestCycle.get().getRequest().getRequestParameters();
        }
    }
}
