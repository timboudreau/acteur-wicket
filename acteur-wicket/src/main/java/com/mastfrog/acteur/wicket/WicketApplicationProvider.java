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

import com.google.inject.Singleton;
import com.mastfrog.giulius.Dependencies;
import java.lang.reflect.InvocationTargetException;
import javax.inject.Inject;
import javax.inject.Provider;
import org.apache.wicket.Application;

/**
 * Injects the application, ensuring it is initialized first.
 *
 * @author Tim Boudreau
 */
@Singleton
class WicketApplicationProvider implements Provider<Application> {
    
    private final Application application;
    
    @Inject
    WicketApplicationProvider(Dependencies deps, WicketConfig config, WicketApplicationInitializer init) throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        application = deps.getInstance(config.applicationClass());
        init.init(application);
    }

    @Override
    public Application get() {
        return application;
    }
}
