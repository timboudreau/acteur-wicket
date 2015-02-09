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

import java.util.Locale;
import org.apache.wicket.Application;

/**
 * Configuration for a Wicket server - the stuff the framework needs to
 * know to launch your application.  You can either implement this and pass
 * it to the constructor of `WicketActeurModule` or you can just pass the
 * application class and it will generate one using the system locale.
 * <p>
 * This should not be considered a stable API - if needed, things like init
 * params (if there's really something Settings is not good enough for) may be
 * added here.
 *
 * @author Tim Boudreau
 */
public interface WicketConfig {
    /**
     * The application class
     * @return The application class
     */
    public Class<? extends Application> applicationClass();
    /**
     * The default locale
     * @return 
     */
    public Locale locale();
}
