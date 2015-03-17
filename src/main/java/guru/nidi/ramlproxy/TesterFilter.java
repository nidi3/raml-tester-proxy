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
import guru.nidi.ramltester.core.RamlReport;
import guru.nidi.ramltester.servlet.ServletRamlRequest;
import guru.nidi.ramltester.servlet.ServletRamlResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

/**
 *
 */
public class TesterFilter implements Filter {
    private final static Logger log = LoggerFactory.getLogger(TesterFilter.class);

    private final RamlProxy<?> proxy;
    private final ReportSaver saver;

    private RamlDefinition ramlDefinition;

    public TesterFilter(RamlProxy<?> proxy, ReportSaver saver) {
        this.proxy = proxy;
        this.saver = saver;
        fetchRamlDefinition();
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
        saver.addReport(report, request, response);
    }

    public boolean handleCommands(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!request.getPathInfo().startsWith("/@@@proxy")) {
            return false;
        }
        final String command = request.getPathInfo().substring(10);
        final PrintWriter writer = response.getWriter();
        switch (command) {
            case "stop":
                writer.write("Stopping proxy");
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            log.info("Stopping proxy");
                            Thread.sleep(100);
                            proxy.close();
                        } catch (Exception e) {
                            log.info("Problem stopping proxy, killing instead: " + e);
                            System.exit(1);
                        }
                    }
                }).start();
                break;
            case "options":
                final BufferedReader in = request.getReader();
                final OptionContainer options;
                final String raw = in.readLine();
                try {
                    options = new OptionContainer(raw.split(" "), false);
                    writer.write(options.equals(proxy.getOptions()) ? "same" : "different");
                } catch (Exception e) {
                    writer.println("illegal options: '" + raw + "'");
                    e.printStackTrace(writer);
                }
                break;
            //TODO rename?
            case "reload":
                fetchRamlDefinition();
                saver.flushReports();
                writer.write("RAML reloaded");
                log.info("RAML reloaded");
                break;
//            case "":
            default:
                log.info("Ignoring unknown command '" + command + "'");
        }
        writer.flush();
        return true;
    }

    private void fetchRamlDefinition() {
        ramlDefinition = proxy.fetchRamlDefinition();
    }
}

