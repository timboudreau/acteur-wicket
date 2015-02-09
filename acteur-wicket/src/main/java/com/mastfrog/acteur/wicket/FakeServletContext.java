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

import com.google.common.collect.Maps;
import com.mastfrog.acteur.server.PathFactory;
import com.mastfrog.acteur.wicket.WicketConfig;
import com.mastfrog.giulius.DeploymentMode;
import com.mastfrog.settings.MutableSettings;
import com.mastfrog.settings.Settings;
import com.mastfrog.settings.SettingsBuilder;
import com.mastfrog.url.Path;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

/**
 *
 * @author Tim Boudreau
 */
final class FakeServletContext implements ServletContext {

    private final WicketConfig config;
    private final PathFactory paths;
    private final MutableSettings settings;
    private final Map<String, Object> attributes = Maps.newConcurrentMap();
    private final DeploymentMode mode;

    @Inject
    FakeServletContext(WicketConfig config, PathFactory paths, Settings settings, DeploymentMode mode) throws IOException {
        this.config = config;
        this.paths = paths;
        this.mode = mode;
        this.settings = new SettingsBuilder().add(settings).buildMutableSettings();
        this.settings.setString("wicket.configuration", mode == DeploymentMode.DEVELOPMENT ? "development" : "production");
    }

    @Override
    public ServletContext getContext(String string) {
        return this;
    }

    @Override
    public int getMajorVersion() {
        return 2;
    }

    @Override
    public int getMinorVersion() {
        return 4;
    }

    @Override
    public String getMimeType(String string) {
        return "text/html; charset=UTF-8";
    }

    @Override
    public Set getResourcePaths(String string) {
        return Collections.emptySet();
    }

    @Override
    public URL getResource(String string) throws MalformedURLException {
        return config.applicationClass().getResource(string);
    }

    @Override
    public InputStream getResourceAsStream(String string) {
        return config.applicationClass().getResourceAsStream(string);
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String string) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public RequestDispatcher getNamedDispatcher(String string) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Servlet getServlet(String string) throws ServletException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Enumeration getServlets() {
        return com.mastfrog.util.collections.CollectionUtils.toEnumeration(Collections.emptyList());
    }

    @Override
    public Enumeration getServletNames() {
        return com.mastfrog.util.collections.CollectionUtils.toEnumeration(Collections.emptyList());
    }

    @Override
    public void log(String string) {
        System.out.println(string);
    }

    @Override
    public void log(Exception excptn, String string) {
        System.out.println(string);
        excptn.printStackTrace();
    }

    @Override
    public void log(String string, Throwable thrwbl) {
        System.out.println(string);
        thrwbl.printStackTrace();
    }

    @Override
    public String getRealPath(String string) {
        return paths.toExternalPath(Path.parse(string)).toString();
    }

    @Override
    public String getServerInfo() {
        return "Acteur 1.6.0-dev";
    }

    @Override
    public String getInitParameter(String string) {
        return settings.getString(string);
    }

    @Override
    public Enumeration getInitParameterNames() {
        return com.mastfrog.util.collections.CollectionUtils.toEnumeration(settings.allKeys());
    }

    @Override
    public Object getAttribute(String string) {
        return attributes.get(string);
    }

    @Override
    public Enumeration getAttributeNames() {
        return com.mastfrog.util.collections.CollectionUtils.toEnumeration(attributes.keySet());
    }

    @Override
    public void setAttribute(String string, Object o) {
        attributes.put(string, o);
    }

    @Override
    public void removeAttribute(String string) {
        attributes.remove(string);
    }

    @Override
    public String getServletContextName() {
        return "";
    }
}
