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
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.tomcat.JarScanner;
import org.apache.tomcat.JarScannerCallback;
import org.junit.AfterClass;
import org.junit.Before;
import org.slf4j.bridge.SLF4JBridgeHandler;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.*;

/**
 *
 */
public abstract class ServerTest {
    private static Tomcat tomcat;
    private static Set<Class<?>> inited = new HashSet<>();

    private final static JarScanner NO_SCAN = new JarScanner() {
        @Override
        public void scan(ServletContext context, ClassLoader classloader, JarScannerCallback callback, Set<String> jarsToSkip) {
        }
    };

    private HttpClient client;

    @Before
    public void setup() {
        client = HttpClientBuilder.create().setSSLHostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String s, SSLSession sslSession) {
                return true;
            }
        }).build();
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
            Context ctx = tomcat.addWebapp("", "src/test");
            ctx.setJarScanner(NO_SCAN);
            ((Host) ctx.getParent()).setAppBase("");

            init(ctx);

            tomcat.start();
            Server server = tomcat.getServer();
            server.start();
        }
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

    protected Map<Object, Object> map(Object... keysValues) {
        final Map<Object, Object> map = new HashMap<>();
        for (int i = 0; i < keysValues.length; i += 2) {
            map.put(keysValues[i], keysValues[i + 1]);
        }
        return map;
    }

    protected List<Object> list(Object... values) {
        return Arrays.asList(values);
    }

    protected String executeGet(String path) throws IOException {
        final HttpGet get = new HttpGet(url(path));
        final HttpResponse response = client.execute(get);
        return EntityUtils.toString(response.getEntity());
    }

    protected String executeGet(RamlProxy<?> proxy, String path) throws Exception {
        proxy.start();
        final String res = executeGet(path);
        proxy.stop();
        return res;
    }
}
