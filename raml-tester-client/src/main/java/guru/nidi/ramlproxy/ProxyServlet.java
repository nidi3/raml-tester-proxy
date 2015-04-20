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

import guru.nidi.ramltester.servlet.ServletRamlRequest;
import guru.nidi.ramltester.servlet.ServletRamlResponse;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 *
 */
public class ProxyServlet extends org.eclipse.jetty.proxy.ProxyServlet.Transparent {
    private final static Logger log = LoggerFactory.getLogger(ProxyServlet.class);
    private final TesterFilter testerFilter;

    public ProxyServlet(TesterFilter testerFilter) {
        this.testerFilter = testerFilter;
    }

    @Override
    public void init() throws ServletException {
        super.init();
        log.info("Proxy started");
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!testerFilter.handleCommands(request, response)) {
            testerFilter.delay();
            super.service(new ServletRamlRequest(request), new ServletRamlResponse(response));
        }
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
        testerFilter.test((ServletRamlRequest) request, (ServletRamlResponse) response);
    }

    @Override
    protected HttpClient newHttpClient() {
        return new HttpClient(new SslContextFactory());
    }

    @Override
    protected void sendProxyRequest(HttpServletRequest request, HttpServletResponse response, Request proxyRequest) {
        final HttpFields headers = proxyRequest.getHeaders();
        headers.remove("Host");
        CommandDecorators.IGNORE_COMMANDS.removeFrom(proxyRequest);
        super.sendProxyRequest(request, response, proxyRequest);
    }
}
