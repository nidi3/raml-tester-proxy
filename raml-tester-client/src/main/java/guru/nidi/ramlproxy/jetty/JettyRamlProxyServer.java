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
package guru.nidi.ramlproxy.jetty;

import guru.nidi.ramlproxy.core.MockServlet;
import guru.nidi.ramlproxy.core.RamlProxyServer;
import guru.nidi.ramlproxy.core.ServerOptions;
import guru.nidi.ramlproxy.core.TesterFilter;
import guru.nidi.ramlproxy.report.ReportSaver;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import javax.servlet.DispatcherType;
import java.util.EnumSet;

/**
 *
 */
public class JettyRamlProxyServer extends RamlProxyServer {
    private final Server server;

    public JettyRamlProxyServer(ServerOptions options, ReportSaver saver) throws Exception {
        super(options, saver);
        server = new Server(options.getPort());
        final ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);
        final TesterFilter testerFilter = new TesterFilter(this, saver);
        final ServletHolder servlet;
        if (options.isMockMode()) {
            servlet = new ServletHolder(new MockServlet(options.getMockDir()));
            context.addFilter(new FilterHolder(testerFilter), "/*", EnumSet.allOf(DispatcherType.class));
        } else {
            servlet = new ServletHolder(new JettyProxyServlet(testerFilter));
            servlet.setInitParameter("proxyTo", options.getTargetUrl());
            servlet.setInitParameter("viaHost", "localhost"); //avoid calling InetAddress.getLocalHost()
        }
        servlet.setInitOrder(1);
        context.addServlet(servlet, "/*");
        server.setStopAtShutdown(true);
        server.start();
    }

    @Override
    public void waitForServer() throws Exception {
        server.join();
    }

    @Override
    protected boolean stop() throws Exception {
        if (server.isStopped() || server.isStopping()) {
            return false;
        }
        server.stop();
        return true;
    }

    @Override
    public boolean isStopped() {
        return server.isStopped();
    }
}
