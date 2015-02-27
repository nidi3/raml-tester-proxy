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
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.proxy.ProxyServlet;
import org.eclipse.jetty.util.ssl.SslContextFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 *
 */
public class TesterProxyServlet extends ProxyServlet.Transparent {

    private final RamlDefinition ramlDefinition;
    private final MultiReportAggregator aggregator;
    private final RamlTesterListener listener;

    public TesterProxyServlet(RamlDefinition ramlDefinition, MultiReportAggregator aggregator, RamlTesterListener listener) {
        this.ramlDefinition = ramlDefinition;
        this.aggregator = aggregator;
        this.listener = listener;

        aggregator.addReport(new RamlReport(ramlDefinition.getRaml()));
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        super.service(new ServletRamlRequest(request), new ServletRamlResponse(response));
    }

    @Override
    protected void onProxyResponseSuccess(HttpServletRequest clientRequest, HttpServletResponse proxyResponse, Response serverResponse) {
        test(clientRequest, proxyResponse);
        super.onProxyResponseSuccess(clientRequest, proxyResponse, serverResponse);
    }

    @Override
    protected void onProxyResponseFailure(HttpServletRequest clientRequest, HttpServletResponse proxyResponse, Response serverResponse, Throwable failure) {
        test(clientRequest, proxyResponse);
        super.onProxyResponseFailure(clientRequest, proxyResponse, serverResponse, failure);
    }

    private void test(HttpServletRequest request, HttpServletResponse response) {
        test((ServletRamlRequest) request, (ServletRamlResponse) response);
    }

    private void test(ServletRamlRequest request, ServletRamlResponse response) {
        final RamlReport report = ramlDefinition.testAgainst(request, response);
        aggregator.addReport(report);
        listener.onViolations(report, request, response);
    }

    @Override
    protected HttpClient newHttpClient() {
        return new HttpClient(new SslContextFactory());
    }

    @Override
    protected void sendProxyRequest(HttpServletRequest request, HttpServletResponse response, Request proxyRequest) {
        proxyRequest.getHeaders().remove("Host");
        super.sendProxyRequest(request, response, proxyRequest);
    }
}
