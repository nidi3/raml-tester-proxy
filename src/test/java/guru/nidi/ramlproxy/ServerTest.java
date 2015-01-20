/*
 * Copyright (C) 2014 Stefan Niederhauser (nidin@gmx.ch)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package guru.nidi.ramlproxy;

import org.apache.catalina.*;
import org.apache.catalina.startup.Tomcat;
import org.apache.tomcat.JarScanner;
import org.apache.tomcat.JarScannerCallback;
import org.junit.AfterClass;
import org.junit.Before;
import org.slf4j.bridge.SLF4JBridgeHandler;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.util.HashSet;
import java.util.Set;

/**
 *
 */
public abstract class ServerTest {
    private static Tomcat tomcat;
    private static Server server;
    private static Context ctx;
    private static Set<Class<?>> inited = new HashSet<>();
    private final static JarScanner NO_SCAN = new JarScanner() {
        @Override
        public void scan(ServletContext context, ClassLoader classloader, JarScannerCallback callback, Set<String> jarsToSkip) {
        }
    };
    private static RamlProxy ramlProxy;
    private SavingRamlTesterListener ramlTesterListener;

    public SavingRamlTesterListener getRamlTesterListener() {
        return ramlTesterListener;
    }

    @Before
    public void initImpl() throws LifecycleException, ServletException {
        if (!inited.contains(getClass())) {
            inited.add(getClass());
            SLF4JBridgeHandler.removeHandlersForRootLogger();
            SLF4JBridgeHandler.install();

            tomcat = new Tomcat();
            tomcat.setPort(serverPort());
            tomcat.setBaseDir(".");
            ctx = tomcat.addWebapp("/", "src/test");
            ctx.setJarScanner(NO_SCAN);
            ((Host) ctx.getParent()).setAppBase("");

            init(ctx);

            tomcat.start();
            server = tomcat.getServer();
            server.start();
        }
    }

    @Before
    public void startProxy() throws Exception {
        ramlTesterListener = new SavingRamlTesterListener();
        ramlProxy = RamlProxy.create(ramlTesterListener,
                "-p", "" + proxyPort(),
                "-t", "localhost:" + serverPort(),
                "-r", "file://src/test/resources/guru/nidi/ramlproxy/simple.raml",
                "-b", "http://nidi.guru/raml/v1");
        ramlProxy.start();
    }

    public void stopProxy() throws Exception {
        ramlProxy.stop();
    }

    protected abstract int serverPort();

    protected abstract int proxyPort();

    protected void init(Context ctx) {
    }

    protected String url(String path) {
        return "http://localhost:" + proxyPort() + "/" + path;
    }

    @AfterClass
    public static void stopTomcat() throws LifecycleException {
        if (tomcat.getServer() != null && tomcat.getServer().getState() != LifecycleState.DESTROYED) {
            if (tomcat.getServer().getState() != LifecycleState.STOPPED) {
                tomcat.stop();
            }
            tomcat.destroy();
        }
    }
}
