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

import guru.nidi.ramltester.MultiReportAggregator;
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
public class RamlProxy<T extends RamlTesterListener> {
    private final Server server;
    private final Thread shutdownHook;
    private final T listener;

    public static void main(String[] args) throws Exception {
        final OptionContainer options = new OptionContainer(args);
        final RamlTesterListener listener = new Reporter(options.getSaveDir(), options.getFileFormat());
        create(listener, options).startAndWait();
    }

    public static <T extends RamlTesterListener> RamlProxy<T> create(T listener, OptionContainer options) throws Exception {
        LogConfigurer.init();
        return new RamlProxy<T>(listener, options);
    }

    public RamlProxy(T listener, OptionContainer options) {
        this.listener = listener;
        server = new Server(options.getPort());
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);
        final RamlDefinition definition = RamlLoaders.absolutely()
                .load(options.getRamlUri())
                .ignoringXheaders(options.isIgnoreXheaders())
                .assumingBaseUri(options.getBaseOrTargetUri());
        final MultiReportAggregator aggregator = new MultiReportAggregator();
        final TesterFilter testerFilter = new TesterFilter(this, definition, aggregator, listener);
        final ServletHolder servlet;
        if (options.isMockMode()) {
            servlet = new ServletHolder(new MockServlet(options.getMockDir()));
            context.addFilter(new FilterHolder(testerFilter), "/*", EnumSet.allOf(DispatcherType.class));
        } else {
            servlet = new ServletHolder(new ProxyServlet(testerFilter));
            servlet.setInitParameter("proxyTo", options.getTargetUrl());
        }
        servlet.setInitOrder(1);
        context.addServlet(servlet, "/*");
        server.setStopAtShutdown(true);
        shutdownHook = shutdownHook(aggregator, listener);
        Runtime.getRuntime().addShutdownHook(shutdownHook);
    }

    public T getListener() {
        return listener;
    }

    public void start() throws Exception {
        server.start();
    }

    public void startAndWait() throws Exception {
        start();
        server.join();
    }

    public void stop() throws Exception {
        server.stop();
        shutdownHook.start();
        shutdownHook.join();
    }

    private static Thread shutdownHook(final MultiReportAggregator aggregator, final RamlTesterListener listener) {
        final Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                listener.onUsage(aggregator);
            }
        });
        thread.setDaemon(true);
        return thread;
    }
}
