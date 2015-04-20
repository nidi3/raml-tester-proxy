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
package guru.nidi.ramlproxy.undertow;

import guru.nidi.ramlproxy.core.MockServlet;
import guru.nidi.ramlproxy.core.RamlProxyServer;
import guru.nidi.ramlproxy.core.ServerOptions;
import guru.nidi.ramlproxy.core.TesterFilter;
import guru.nidi.ramlproxy.report.ReportSaver;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.servlet.util.ImmediateInstanceFactory;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.Servlet;
import java.net.Socket;

import static io.undertow.servlet.Servlets.*;

/**
 *
 */
public class UndertowRamlProxyServer extends RamlProxyServer {
    private final Undertow server;

    public UndertowRamlProxyServer(final ServerOptions options, ReportSaver saver) throws Exception {
        super(options, saver);
        final TesterFilter testerFilter = new TesterFilter(this, saver);
        final DeploymentInfo deployment = deployment()
                .setClassLoader(getClass().getClassLoader())
                .setDeploymentName("raml-tester-mock")
                .setContextPath("/")
                .addServlet(servlet("mock", MockServlet.class,
                        new ImmediateInstanceFactory<Servlet>(new MockServlet(options.getMockDir())))
                        .addMapping("/*"))
                .addFilter(filter("tester", TesterFilter.class, new ImmediateInstanceFactory<Filter>(testerFilter)))
                .addFilterServletNameMapping("tester", "mock", DispatcherType.REQUEST);
        final DeploymentManager manager = defaultContainer().addDeployment(deployment);
        manager.deploy();

        HttpHandler servletHandler = manager.start();
        final Undertow.Builder builder = Undertow.builder()
                .setIoThreads(2)
                .addHttpListener(options.getPort(), "localhost");
        if (options.isMockMode()) {
            builder.setHandler(servletHandler);
        } else {
            builder.setHandler(new UndertowProxyHandler(options.getTargetUrl(),testerFilter));
        }
        server = builder.build();

//                        .setHandler(new HttpHandler() {
//                            @Override
//                            public void handleRequest(final HttpServerExchange exchange) throws Exception {
//                                exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
//                                exchange.getResponseSender().send("Hello World");
//                            }
//                        }).build();
        server.start();
//        server = new Server(options.getPort());
//        final ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
//        context.setContextPath("/");
//        server.setHandler(context);
//        final TesterFilter testerFilter = new TesterFilter(this, saver);
//        final ServletHolder servlet;
//        if (options.isMockMode()) {
//            servlet = new ServletHolder(new MockServlet(options.getMockDir()));
//            context.addFilter(new FilterHolder(testerFilter), "/*", EnumSet.allOf(DispatcherType.class));
//        } else {
//            servlet = new ServletHolder(new ProxyServlet(testerFilter));
//            servlet.setInitParameter("proxyTo", options.getTargetUrl());
//            servlet.setInitParameter("viaHost", "localhost"); //avoid calling InetAddress.getLocalHost()
//        }
//        servlet.setInitOrder(1);
//        context.addServlet(servlet, "/*");
//        server.setStopAtShutdown(true);
//        server.start();
    }

    @Override
    public synchronized void waitForServer() throws Exception {
        wait();
    }

    @Override
    protected synchronized boolean stop() throws Exception {
        if (isStopped()) {
            return false;
        }
        server.stop();
        notify();
        return true;
    }

    @Override
    public boolean isStopped() {
        try (final Socket socket = new Socket("localhost", options.getPort())) {
            return false;
        } catch (Exception e) {
            return true;
        }
    }
}
