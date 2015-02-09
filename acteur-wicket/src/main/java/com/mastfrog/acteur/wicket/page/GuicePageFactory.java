package com.mastfrog.acteur.wicket.page;

import com.google.inject.Singleton;
import com.mastfrog.giulius.Dependencies;
import com.mastfrog.guicy.scope.ReentrantScope;
import com.mastfrog.util.Exceptions;
import com.mastfrog.util.thread.QuietAutoCloseable;
import java.lang.reflect.Constructor;
import java.util.concurrent.ConcurrentMap;
import javax.inject.Inject;
import org.apache.wicket.IPageFactory;
import org.apache.wicket.request.component.IRequestablePage;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.lang.Generics;

/**
 * Instantiates pages using Guice
 *
 * @author Tim Boudreau
 */
@Singleton
public class GuicePageFactory implements IPageFactory {

    private final ReentrantScope requestScope;
    private final ConcurrentMap<String, Boolean> pageToBookmarkableCache = Generics.newConcurrentHashMap();
    private final Dependencies deps;
    private final IPageInstantiationInterceptor interceptor;
    private final boolean isNoOp;

    @Inject
    public GuicePageFactory(Dependencies deps, IPageInstantiationInterceptor interceptor, ReentrantScope scope) {
        requestScope = scope;
        this.deps = deps;
        this.interceptor = interceptor;
        this.isNoOp = interceptor instanceof NoOpPageInstantiationInterceptor;
    }

    @Override
    public <C extends IRequestablePage> C newPage(Class<C> type) {
        C result = deps.getInstance(type);
        if (!isNoOp) {
            try {
                interceptor.onInstantiation(result);
            } catch (Exception ex) {
                return Exceptions.chuck(ex);
            }
        }
        return result;
    }

    @Override
    public <C extends IRequestablePage> C newPage(Class<C> type, PageParameters pp) {
        try (QuietAutoCloseable clos = requestScope.enter(pp)) {
            return newPage(type);
        }
    }

    @Override
    public <C extends IRequestablePage> boolean isBookmarkable(Class<C> pageClass) {
        Boolean bookmarkable = pageToBookmarkableCache.get(pageClass.getName());
        if (bookmarkable == null) {
            try {
                if (pageClass.getDeclaredConstructor(new Class[]{}) != null) {
                    bookmarkable = Boolean.TRUE;
                }
            } catch (Exception ignore) {
                try {
                    if (pageClass.getDeclaredConstructor(new Class[]{PageParameters.class}) != null) {
                        bookmarkable = true;
                    }
                } catch (Exception ignored) {
                    // @Inject constructors are bookmarkable
                    for (Constructor<?> c : pageClass.getDeclaredConstructors()) {
                        if (c.getAnnotation(com.google.inject.Inject.class) != null) {
                            bookmarkable = true;
                            break;
                        }
                        if (c.getAnnotation(javax.inject.Inject.class) != null) {
                            bookmarkable = true;
                            break;
                        }
                    }
                }
            }
            if (bookmarkable == null) {
                bookmarkable = false;
            }
            Boolean tmpBookmarkable = pageToBookmarkableCache.putIfAbsent(pageClass.getName(), bookmarkable);
            if (tmpBookmarkable != null) {
                bookmarkable = tmpBookmarkable;
            }
        }
        return bookmarkable;
    }
}
