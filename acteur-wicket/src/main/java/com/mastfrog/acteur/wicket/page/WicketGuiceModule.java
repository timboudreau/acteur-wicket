package com.mastfrog.acteur.wicket.page;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.mastfrog.guicy.scope.ReentrantScope;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.wicket.IPageFactory;
import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;

/**
 * Guice module which binds commonly used items for injection into Page instances.
 *
 * @author Tim Boudreau
 */
public class WicketGuiceModule implements Module {

    private final ReentrantScope scope;

    public WicketGuiceModule(ReentrantScope scope) {
        this.scope = scope;
    }

    @Override
    public void configure(Binder binder) {
        // Make sure a PageParameters is always available, for instantiating
        // pages
        binder.bind(PageParameters.class).toProvider(
                scope.provider(PageParameters.class, new EmptyPageParametersProvider()));
        // Our custom page factory that will use Guice to create page instances
        // It is injected into the Application instance
        binder.bind(IPageFactory.class).to(GuicePageFactory.class);
        // Request ids give us a way to trace a request across database calls,
        // etc..
        // Other types that are useful to inject
        scope.bindTypes(binder, ServletRequest.class, ServletResponse.class, RequestCycle.class, HttpServletRequest.class, HttpServletResponse.class);
        binder.bind(IRequestParameters.class).toProvider(RequestParametersProvider.class);
        binder.bind(Request.class).toProvider(RequestProvider.class);
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

    @Singleton
    private static final class RequestProvider implements Provider<Request> {

        @Override
        public Request get() {
            return RequestCycle.get().getRequest();
        }
    }
}
