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
package guru.nidi.ramlproxy.core;

import guru.nidi.ramlproxy.report.ReportSaver;
import guru.nidi.ramltester.RamlDefinition;
import guru.nidi.ramltester.core.RamlReport;
import guru.nidi.ramltester.servlet.ServletRamlRequest;
import guru.nidi.ramltester.servlet.ServletRamlResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 *
 */
public class TesterFilter implements Filter, CommandContext {
    private final static Logger log = LoggerFactory.getLogger(TesterFilter.class);
    public static final String COMMAND_PATH = "/@@@proxy";

    private final RamlProxyServer proxy;
    private final ReportSaver saver;

    private RamlDefinition ramlDefinition;

    public TesterFilter(RamlProxyServer proxy, ReportSaver saver) {
        this.proxy = proxy;
        this.saver = saver;
        reloadRamlDefinition();
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
            delay();
            chain.doFilter(ramlReq, ramlRes);
            test(ramlReq, ramlRes);
        }
    }

    @Override
    public void destroy() {
    }

    public void test(ServletRamlRequest request, ServletRamlResponse response) {
        try {
            final RamlReport report = ramlDefinition.testAgainst(request, response);
            saver.addReport(report, request, response);
        } catch (Exception e) {
            try {
                saver.addReport(RamlReport.fromException(ramlDefinition.getRaml(), e), request, response);
            } catch (Exception e2) {
                System.err.println("Problem checking raml '" + ramlDefinition.getRaml().getTitle() + "'");
                e.printStackTrace();
            }
        }
    }

    public boolean handleCommands(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!request.getPathInfo().startsWith(COMMAND_PATH) || CommandDecorators.IGNORE_COMMANDS.isSet(request)) {
            return false;
        }
        CommandDecorators.ALLOW_ORIGIN.set(request, response);
        final String commandStr = request.getPathInfo().substring(10);
        final PrintWriter writer = response.getWriter();
        final Command command = Command.byName(commandStr);
        if (command == null) {
            log.info("Ignoring unknown command '" + commandStr + "'");
        } else {
            command.apply(response);
            command.execute(this, writer);
        }
        if (CommandDecorators.CLEAR_REPORTS.isSet(request)) {
            Command.CLEAR_REPORTS.execute(this, writer);
        }
        if (CommandDecorators.CLEAR_USAGE.isSet(request)) {
            Command.CLEAR_USAGE.execute(this, writer);
        }
        writer.flush();
        return true;
    }

    public void delay() {
        proxy.delay();
    }

    @Override
    public void reloadRamlDefinition() {
        ramlDefinition = proxy.fetchRamlDefinition();
    }

    @Override
    public RamlReport validateRaml() {
        return ramlDefinition.validate();
    }

    @Override
    public void stopProxy() throws Exception {
        proxy.close();
    }

    @Override
    public ReportSaver getSaver() {
        return saver;
    }
}
