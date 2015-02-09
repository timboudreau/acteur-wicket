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

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import javax.inject.Inject;
import javax.servlet.ServletContext;
import org.apache.wicket.Application;
import org.apache.wicket.IPageFactory;
import org.apache.wicket.ThreadContext;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.WicketFilter;
import org.apache.wicket.session.ISessionStore;
import org.apache.wicket.util.IProvider;

/**
 * Class which performs pre-initialization on the application - does the
 * same things that WicketFilter.init() does in a servlet-based container.
 *
 * @author Tim Boudreau
 */
public class WicketApplicationInitializer implements IProvider<ISessionStore> {
    private final IPageFactory factory;
    private final ServletContext ctx;
    private final WicketFilter filter;
    private final ISessionStore store;
    
    @Inject
    WicketApplicationInitializer(IPageFactory factory, ServletContext ctx, WicketFilter filter, ISessionStore store) {
        this.factory = factory;
        this.ctx = ctx;
        this.filter = filter;
        this.store = store;
    }
    
    protected void init(Application application) throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        WebApplication wa = (WebApplication) application;
        wa.setWicketFilter(filter);
        wa.setServletContext(ctx);
        wa.setSessionStoreProvider(this);
        ThreadContext.setApplication(application);
        application.setName(application.getClass().getName());
        application.initApplication();
        wa.setSessionStoreProvider(this);
        Field field = Application.class.getDeclaredField("pageFactory");
        field.setAccessible(true);
        field.set(application, factory);
        Method logStarted = WebApplication.class.getDeclaredMethod("logStarted");
        logStarted.setAccessible(true);
        logStarted.invoke(application);
    }

    @Override
    public ISessionStore get() {
        return store;
    }
}
