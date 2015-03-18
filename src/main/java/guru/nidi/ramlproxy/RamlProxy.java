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

import guru.nidi.ramltester.RamlDefinition;
import guru.nidi.ramltester.RamlLoaders;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import javax.servlet.DispatcherType;
import java.util.EnumSet;

/**
 *
 */
public class RamlProxy implements AutoCloseable {
    private final Server server;
    private final Thread shutdownHook;
    private final ReportSaver saver;
    private final OptionContainer options;

    public static void main(String[] args) throws Exception {
        final OptionContainer options = OptionContainer.fromCli(args);
        final ReportSaver saver = new Reporter(options.getSaveDir(), options.getFileFormat());
        final RamlProxy proxy = create(saver, options);
        proxy.waitForServer();
    }

    public static RamlProxy create(ReportSaver saver, OptionContainer options) throws Exception {
        LogConfigurer.init();
        return new RamlProxy(saver, options);
    }

    public RamlProxy(ReportSaver saver, OptionContainer options) throws Exception {
        this.saver = saver;
        this.options = options;
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
            servlet = new ServletHolder(new ProxyServlet(testerFilter));
            servlet.setInitParameter("proxyTo", options.getTargetUrl());
            servlet.setInitParameter("viaHost", "localhost"); //avoid calling InetAddress.getLocalHost()
        }
        servlet.setInitOrder(1);
        context.addServlet(servlet, "/*");
        server.setStopAtShutdown(true);
        shutdownHook = shutdownHook(saver);
        Runtime.getRuntime().addShutdownHook(shutdownHook);
        server.start();
    }

    public RamlDefinition fetchRamlDefinition() {
        return RamlLoaders.absolutely()
                .load(options.getRamlUri())
                .ignoringXheaders(options.isIgnoreXheaders())
                .assumingBaseUri(options.getBaseOrTargetUri());
    }

    public ReportSaver getSaver() {
        return saver;
    }

    public OptionContainer getOptions() {
        return options;
    }

    public void waitForServer() throws Exception {
        server.join();
    }

    @Override
    public void close() throws Exception {
        if (!server.isStopped() && !server.isStopping()) {
            server.stop();
            shutdownHook.start();
            shutdownHook.join();
        }
    }

    public boolean isStopped() {
        return server.isStopped();
    }

    private static Thread shutdownHook(final ReportSaver saver) {
        final Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                saver.flushUsage();
            }
        });
        thread.setDaemon(true);
        return thread;
    }
}
