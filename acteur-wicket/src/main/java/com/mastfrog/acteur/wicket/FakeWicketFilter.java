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

import com.google.inject.Provider;
import com.mastfrog.acteur.wicket.WicketConfig;
import com.mastfrog.giulius.ShutdownHookRegistry;
import javax.inject.Inject;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import org.apache.wicket.Application;
import org.apache.wicket.protocol.http.IWebApplicationFactory;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.WicketFilter;

/**
 * Wicket's API requires that a WicketFilter instance be present even when
 * it is not running in a servlet container, so this is a mock that allows
 * Wicket to start.
 *
 * @author Tim Boudreau
 */
final class FakeWicketFilter extends WicketFilter implements IWebApplicationFactory, Runnable {
    private final Provider<Application> app;
    private final WicketConfig config;
    private final FilterConfig filterConfig;

    @Inject
    FakeWicketFilter(Provider<Application> app, ShutdownHookRegistry reg, WicketConfig config, FilterConfig filterConfig) throws ServletException {
        this.config = config;
        this.app = app;
        this.filterConfig = filterConfig;
        reg.add(this);
    }
    
    @Override
    protected WebApplication getApplication() {
        return (WebApplication) app.get();
    }

    @Override
    public String getFilterPath() {
        return "/";
    }

    @Override
    public FilterConfig getFilterConfig() {
        return filterConfig;
    }

    @Override
    protected IWebApplicationFactory getApplicationFactory() {
        return super.getApplicationFactory(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected ClassLoader getClassLoader() {
        return config.applicationClass().getClassLoader();
    }

    @Override
    public WebApplication createApplication(WicketFilter filter) {
        return (WebApplication) app.get();
    }

    @Override
    public void run() {
        destroy();
    }

    @Override
    public void destroy(WicketFilter filter) {
        filter.destroy();
    }

    @Override
    public void init(boolean isServlet, FilterConfig filterConfig) throws ServletException {
        throw new UnsupportedOperationException("Do not call - we are not in a servlet container");
    }
}
