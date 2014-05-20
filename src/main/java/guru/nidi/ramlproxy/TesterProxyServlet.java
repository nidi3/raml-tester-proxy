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
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.proxy.ProxyServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 *
 */
public class TesterProxyServlet extends ProxyServlet.Transparent {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final RamlDefinition ramlDefinition;
    private final File saveDir;
    private final AtomicLong id = new AtomicLong();
    private final String startup = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").format(new Date());

    public TesterProxyServlet(String proxyTo, RamlDefinition ramlDefinition, File saveDir) {
        super(proxyTo.startsWith("http") ? proxyTo : ("http://" + proxyTo), "");
        this.ramlDefinition = ramlDefinition;
        this.saveDir = saveDir;
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        final ServletRamlRequest ramlRequest = new ServletRamlRequest(request);
        final ServletRamlResponse ramlResponse = new ServletRamlResponse(response);
        super.service(ramlRequest, ramlResponse);
    }

    @Override
    protected void onResponseSuccess(HttpServletRequest request, HttpServletResponse response, Response proxyResponse) {
        test(request, response);
        super.onResponseSuccess(request, response, proxyResponse);
    }

    @Override
    protected void onResponseFailure(HttpServletRequest request, HttpServletResponse response, Response proxyResponse, Throwable failure) {
        test(request, response);
        super.onResponseFailure(request, response, proxyResponse, failure);
    }

    private void test(HttpServletRequest request, HttpServletResponse response) {
        test((ServletRamlRequest) request, (ServletRamlResponse) response);
    }

    private void test(ServletRamlRequest request, ServletRamlResponse response) {
        final RamlReport report = ramlDefinition.testAgainst(request, response);
        if (!report.isEmpty()) {
            final long idValue = id.incrementAndGet();
            log.error("<{}> {}\n           Request:  {}\n           Response: {}", idValue, formatRequest(request), report.getRequestViolations(), report.getResponseViolations());
            writeToFile(idValue, report, request, response);
        }
    }

    private void writeToFile(long idValue, RamlReport report, ServletRamlRequest request, ServletRamlResponse response) {
        if (saveDir == null) {
            return;
        }
        try {
            try (FileOutputStream out = new FileOutputStream(new File(saveDir, "raml-violation-" + startup + "--" + idValue))) {
                out.write(("Request violations: " + report.getRequestViolations() + "\n\n").getBytes());
                out.write((formatRequest(request) + "\n").getBytes());
                out.write((formatHeaders(request.getHeaderMap()) + "\n").getBytes());
                out.write((request.getContent() == null ? "No content" : request.getContent()).getBytes());
                out.write(("\n\n\nResponse violations: " + report.getResponseViolations() + "\n\n").getBytes());
                out.write((formatHeaders(response.getHeaderMap()) + "\n").getBytes());
                out.write((response.getContent() == null ? "No content" : response.getContent()).getBytes());
            }
        } catch (IOException e) {
            log.error("Problem writing error file", e);
        }
    }

    protected String formatRequest(ServletRamlRequest request) {
        return request.getMethod() + " " + request.getRequestURL() +
                (request.getQueryString() == null ? "" : ("?" + request.getQueryString())) +
                " from " + request.getRemoteHost();

    }

    private String formatHeaders(Map<String, String[]> headerMap) {
        String res = "";
        for (Map.Entry<String, String[]> entry : headerMap.entrySet()) {
            for (String value : entry.getValue()) {
                res += entry.getKey() + ": " + value + "\n";
            }
        }
        return res;
    }

}
