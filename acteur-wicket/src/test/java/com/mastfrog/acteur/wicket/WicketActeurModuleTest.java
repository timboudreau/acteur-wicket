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

import com.mastfrog.acteur.server.PathFactory;
import com.mastfrog.acteur.server.ServerBuilder;
import com.mastfrog.acteur.server.ServerModule;
import com.mastfrog.acteur.util.Server;
import com.mastfrog.acteur.wicket.borrowed.NavomaticApplication;
import static com.mastfrog.giulius.SettingsBindings.*;
import com.mastfrog.guicy.scope.ReentrantScope;
import com.mastfrog.settings.Settings;
import com.mastfrog.settings.SettingsBuilder;

/**
 * Starts a one-page application borrowed from wicket-examples.
 *
 * @author Tim Boudreau
 */
public class WicketActeurModuleTest {

    public static void main(String[] args) throws Exception {
        // All of these settings can be overridden by
        // values in /etc/acteur-wicket.properties, /opt/local/etc/acteur-wicket.properties,
        // ~/acteur-wicket.properties and ./acteur-wicket.properties in that order,
        // or by command-line arguments in the form, e.g. --port 8080
        Settings settings = new SettingsBuilder("acteur-wicket")
                .add(ServerModule.PORT, "5757") // set the default port
                .add(PathFactory.EXTERNAL_PORT, "5757") // ensures redirects go to the local port
                .addFilesystemAndClasspathLocations()
                .parseCommandLineArguments(args)
                .build();

        Server server = new ServerBuilder("acteur-wicket")
                .add(HomePageApplicationModule.class)
                .add(settings)
                .enableOnlyBindingsFor(BOOLEAN, INT, LONG) // minor optimization
                .build();
        server.start();
    }

    static class HomePageApplicationModule extends WicketActeurModule {

        // ServerBuilder can instantiate a module which takes a ReentrantScope
        // Settings or both in its constructor - the scope is part of the way
        // Acteur creates dynamic constructor arguments using request contents
        HomePageApplicationModule(ReentrantScope scope) {
            super(NavomaticApplication.class, scope);
        }
    }
}
