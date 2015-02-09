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
class GuicePageFactory implements IPageFactory {

    private final ReentrantScope requestScope;
    private final ConcurrentMap<String, Boolean> pageToBookmarkableCache = Generics.newConcurrentHashMap();
    private final Dependencies deps;

    @Inject
    public GuicePageFactory(Dependencies deps, ReentrantScope scope) {
        requestScope = scope;
        this.deps = deps;
    }

    @Override
    public <C extends IRequestablePage> C newPage(Class<C> type) {
        return deps.getInstance(type);
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
