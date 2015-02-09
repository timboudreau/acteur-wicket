acteur-wicket
=============

Run Wicket applications without a servlet container or any servlets at all.

Experimental integration of [Apache Wicket](http://wicket.apache.org) into 
[Acteur](http://timboudreau.com/blog/updatedActeur/read);  at this point not very
developed, but works to run a single-page test application
(run the class `WicketActeurModuleTest` in the test packages).

This means running Wicket applications without any servlets or servlet containers,
using [Netty](http://netty.io) and [Acteur](http://timboudreau.com/blog/updatedActeur/read)
to handle the plumbing of network I/O and HTTP protocol.

Plenty to do on it, but the basics work, so it has some promise.

Unfortunately, most Wicket applications routinely cast to `HttpServletRequest`
and `ServletWebRequest`;  even Wicket's internals assume a `WicketFilter` is
present (we mock this with `FakeWicketFilter`).  Things that make the assumption
that they're running in a servlet container and blindly cast will fail.


Why?
----

 * Application startup times of 1-5 seconds, for one thing
 * If you use Acteur's API, the ability to asynchronously write a response a chunk at a time, getting called
back when one chunk has been flushed to the socket.
 * Microscopic memory footprint, as compared with most application servers
(wicket definitely adds memory requirements;  a small Acteur application such
as [tiny-maven-proxy](https://github.com/timboudreau/tiny-maven-proxy) can run
in 7Mb).
 * No XML - and not because something is generating it under the hood - your
server is a program and you can run it, period.  Flexible configuration can be
done using properties files (uses [Giulius](https://github.com/timboudreau/giulius) under the hood)


Usage
-----

Startup is done the same way it is done with Acteur - just have a main method,
create a settings with any configuration you want to load, create a ServerBuilder
with it, build your server and start it.

Unlike with servlet containers, there is *no* XML, deployment descriptors
or similar (if you think that's a bad thing, this project is not for you).

Here is the startup code for the example, 
[WicketActeurModuleTest](https://github.com/timboudreau/acteur-wicket/blob/master/acteur-wicket/src/test/java/com/mastfrog/acteur/wicket/WicketActeurModuleTest.java#L42) in the
tests classes:

```java
public static void main(String[] args) throws Exception {
    Settings settings = new SettingsBuilder("acteur-wicket")
            .add(PathFactory.EXTERNAL_PORT, "5757")
            .add(ServerModule.PORT, "5757")
            .addFilesystemAndClasspathLocations()
            .parseCommandLineArguments(args)
            .buildMutableSettings();

    Server server = new ServerBuilder("acteur-wicket")
            .add(WAM.class)
            .add(settings)
            .build();
    server.start();
}

static class WAM extends WicketActeurModule {

    WAM(ReentrantScope scope) {
        super(HomePageApplication.class, scope);
    }
}
```


The Code
--------

The meat of the code that is not adapters for other things is in

 * `WicketApplicationInitializer` - does the things WicketFilter does to get
the application ready for use, does a few hacks to set the page factory
 * `WicketActeur` - dispatch acteur which invokes the `RequestCycle`
 * `EnsureSessionId` - acteur called before `WicketActeur` to ensure every request has a session id
 * `ActeurSessionStore` - session storage - for now, just maintains a concurrent hash map, but the backing
storage could be made pluggable


Things That Are Different
-------------------------

 * Acteur uses Guice, so this does too.  Page and application are instantiated with Guice.  Note
this does not use the Wicket-Guice project - only pages and the application use Guice directly
(wicket guice mainly does field injection, which is of limited usefulness in a framework that does
most of its work in constructors).  Any class that Acteur allows you to inject can be injected
into the constructor of a page or the application.  `PageParameters` work normally.
 * Acteur won't have built-in support for HTTPs until the cleaner API in Netty 5 is available, so
all requests are assumed to be HTTP.  The recommendation with Acteur is to do your HTTPs via a
reverse proxy such as NginX, which also lets you do clean zero-downtime redeployment by starting
the new application on another port and then updating NginX's configuration and shutting down the
old one
 * The equivalent of a servlet/filter path can be set by setting the setting `basePath` (or passing
`--basepath /some/path` in the above example


To-Do
-----

 * Test with some real applications and fix things
 * [acteur-resources](https://github.com/timboudreau/acteur/tree/master/acteur-resources) has good 
support for serving static files that should be integrated - right now these need to be mounted with code

Packaging And Deployment
------------------------

Use Acteur's [maven-merge-configuration](https://github.com/timboudreau/tiny-maven-proxy/blob/master/pom.xml#L103)
plugin to build a merged JAR which automatically coalesces Acteur's generated files and things like
`META-INF/services` files (say, if you have need JDBC drivers) - see the link above - configuration consists
of setting the main class name.

Then run it with good old `java -jar`.
