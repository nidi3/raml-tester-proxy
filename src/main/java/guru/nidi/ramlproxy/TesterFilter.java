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
import guru.nidi.ramltester.core.RamlReport;
import guru.nidi.ramltester.servlet.ServletRamlRequest;
import guru.nidi.ramltester.servlet.ServletRamlResponse;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 *
 */
public class TesterFilter implements Filter {
    private final RamlProxy<?> proxy;
    private final RamlDefinition ramlDefinition;
    private final MultiReportAggregator aggregator;
    private final RamlTesterListener listener;

    public TesterFilter(RamlProxy<?> proxy, RamlDefinition ramlDefinition, MultiReportAggregator aggregator, RamlTesterListener listener) {
        this.proxy = proxy;
        this.ramlDefinition = ramlDefinition;
        this.aggregator = aggregator;
        this.listener = listener;
        aggregator.addReport(new RamlReport(ramlDefinition.getRaml()));
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        final HttpServletRequest servletReq = (HttpServletRequest) request;
        final HttpServletResponse servletRes = (HttpServletResponse) response;
        if (!handleCommands(servletReq, servletRes)) {
            final ServletRamlRequest ramlReq = new ServletRamlRequest(servletReq);
            final ServletRamlResponse ramlRes = new ServletRamlResponse(servletRes);
            chain.doFilter(ramlReq, ramlRes);
            test(ramlReq, ramlRes);
        }
    }

    @Override
    public void destroy() {
    }

    public void test(ServletRamlRequest request, ServletRamlResponse response) {
        final RamlReport report = ramlDefinition.testAgainst(request, response);
        aggregator.addReport(report);
        listener.onViolations(report, request, response);
    }

    public boolean handleCommands(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!request.getPathInfo().startsWith("/@@@proxy")) {
            return false;
        }
        final String command = request.getPathInfo().substring(10);
        switch (command) {
            case "stop":
                final PrintWriter writer = response.getWriter();
                writer.write("Stopping proxy");
                writer.flush();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            System.out.println("Stopping proxy");
                            Thread.sleep(100);
                            proxy.stop();
                        } catch (Exception e) {
                            System.out.println("Problem stopping proxy, killing instead: " + e);
                            System.exit(1);
                        }
                    }
                }).start();
                break;
            default:
                System.out.println("Ignoring unknown command '" + command + "'");
        }
        return true;
    }
}

