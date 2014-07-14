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
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class Application {
    private final static Logger log = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) throws Exception {
        LogConfigurer.init();
        final OptionContainer optionContainer = new OptionContainer(args);
        start(optionContainer);
    }

    private static void start(OptionContainer options) throws Exception {
        final Server server = new Server(options.getPort());
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);
        final RamlDefinition definition = RamlLoaders
                .loadFromUri(options.getRamlUri())
                .assumingBaseUri(options.getBaseUri());
        final MultiReportAggregator aggregator = new MultiReportAggregator();
        final Reporter reporter = new Reporter(options.getSaveDir());
        final ServletHolder servlet = new ServletHolder(new TesterProxyServlet(options.getTarget(), definition, aggregator, reporter));
        servlet.setInitOrder(1);
        context.addServlet(servlet, "/*");
        server.setStopAtShutdown(true);
        Runtime.getRuntime().addShutdownHook(shutdownHook(aggregator, reporter));
        server.start();
        log.info("Proxy started");
        server.join();
    }

    private static Thread shutdownHook(final MultiReportAggregator aggregator, final Reporter reporter) {
        final Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                reporter.reportUsage(aggregator);
            }
        });
        thread.setDaemon(true);
        return thread;
    }
}
