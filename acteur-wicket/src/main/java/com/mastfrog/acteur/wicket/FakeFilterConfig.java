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

import com.mastfrog.settings.Settings;
import java.util.Enumeration;
import javax.inject.Inject;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;

/**
 * A fake filter config for use with FakeWicketFilter to make things work.
 *
 * @author Tim Boudreau
 */
final class FakeFilterConfig implements FilterConfig {
    private final ServletContext ctx;
    private final Settings settings;

    @Inject
    FakeFilterConfig(ServletContext ctx, Settings settings) {
        this.ctx = ctx;
        this.settings = settings;
    }

    @Override
    public String getFilterName() {
        return "wicket";
    }

    @Override
    public ServletContext getServletContext() {
        return ctx;
    }

    @Override
    public String getInitParameter(String string) {
        return settings.getString(string);
    }

    @Override
    public Enumeration getInitParameterNames() {
        return com.mastfrog.util.collections.CollectionUtils.toEnumeration(settings.allKeys());
    }
}
