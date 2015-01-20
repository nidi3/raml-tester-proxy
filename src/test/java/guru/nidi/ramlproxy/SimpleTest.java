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

import com.fasterxml.jackson.databind.ObjectMapper;
import guru.nidi.ramlproxy.SavingRamlTesterListener.ReportInfo;
import guru.nidi.ramltester.core.RamlViolations;
import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.junit.Before;
import org.junit.Test;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.*;

/**
 *
 */
public class SimpleTest extends ServerTest {
    private CloseableHttpClient client;

    @Override
    protected int serverPort() {
        return 8080;
    }

    @Override
    protected int proxyPort() {
        return 8090;
    }

    @Before
    public void setup() {
        client = HttpClientBuilder.create().setSSLHostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String s, SSLSession sslSession) {
                return true;
            }
        }).build();

    }

    @Test
    public void simpleOk() throws Exception {
        final RamlProxy<SavingRamlTesterListener> proxy = RamlProxy.create(new SavingRamlTesterListener(), new OptionContainer(proxyPort(),
                "http://localhost:" + serverPort(), "file://src/test/resources/guru/nidi/ramlproxy/simple.raml", "http://nidi.guru/raml/v1"));
        proxy.start();

        final HttpGet get = new HttpGet(url("data"));
        final CloseableHttpResponse response = client.execute(get);
        final String res = EntityUtils.toString(response.getEntity());

        proxy.stop();

        assertEquals("42", res);

        final List<ReportInfo> reports = proxy.getListener().getReports();
        assertEquals(1, reports.size());

        assertTrue(reports.get(0).getReport().getRequestViolations().isEmpty());
        assertTrue(reports.get(0).getReport().getResponseViolations().isEmpty());
    }

    private Reporter reporterTest(ReportFormat format) throws Exception {
        final Reporter reporter = new Reporter(new File("target"), format);
        final RamlProxy<Reporter> proxy = RamlProxy.create(reporter, new OptionContainer(proxyPort(),
                "http://localhost:" + serverPort(), "file://src/test/resources/guru/nidi/ramlproxy/simple.raml", "http://nidi.guru/raml/v1"));
        proxy.start();

        final HttpGet get = new HttpGet(url("data?param=1"));
        final CloseableHttpResponse response = client.execute(get);
        final String res = EntityUtils.toString(response.getEntity());

        proxy.stop();

        assertEquals("illegal json", res);
        return reporter;
    }

    @Test
    public void reporterText() throws Exception {
        final Reporter reporter = reporterTest(ReportFormat.TEXT);
        assertTrue(reporter.usageFile("simple").exists());
    }

    @Test
    public void reporterJson() throws Exception {
        final Reporter reporter = reporterTest(ReportFormat.JSON);
        final ObjectMapper mapper = new ObjectMapper();
        assertEquals(map("context", "simple",
                        "unused", map(
                                "request headers", list("head in GET /data"),
                                "form parameters", list("a in POST /data (application/x-www-form-urlencoded)"),
                                "response headers", list("rh in GET /data -> 200"),
                                "response codes", list("201 in GET /data"),
                                "resources", list("/other"),
                                "query parameters", list("q in GET /data"),
                                "actions", list("POST /data"))),
                mapper.readValue(reporter.usageFile("simple"), Map.class));

        final Map actual = mapper.readValue(reporter.violationsFile(1), Map.class);
        final List<String> resVio = (List<String>) actual.get("response violations");
        assertThat(resVio.get(0), startsWith("Body does not match schema for action(GET /data) response(200) mime-type('application/json')\nContent: illegal json\n"));
        assertEquals(map("id", 1,
                        "request", "GET http://localhost:8090/data?param=1 from 127.0.0.1",
                        "request headers", map(
                                "Connection", list("keep-alive"),
                                "User-Agent", ((Map) actual.get("request headers")).get("User-Agent"),
                                "Host", list("localhost:8090"),
                                "Accept-Encoding", list("gzip,deflate")),
                        "request violations", list("Query parameter 'param' on action(GET /data) is not defined"),
                        "response", "illegal json",
                        "response headers", map(
                                "Server", list("Apache-Coyote/1.1"),
                                "Date", ((Map) actual.get("response headers")).get("Date"),
                                "Content-Type", list("application/json;charset=ISO-8859-1")),
                        "response violations", resVio),
                actual);
    }

    @Test
    public void simpleNok() throws Exception {
        final RamlProxy<SavingRamlTesterListener> proxy = RamlProxy.create(new SavingRamlTesterListener(), new OptionContainer(proxyPort(),
                "http://localhost:" + serverPort(), "file://src/test/resources/guru/nidi/ramlproxy/simple.raml", "http://nidi.guru/raml/v1"));
        proxy.start();

        final HttpGet get = new HttpGet(url("data?param=1"));
        final CloseableHttpResponse response = client.execute(get);
        final String res = EntityUtils.toString(response.getEntity());

        proxy.stop();

        assertEquals("illegal json", res);

        final List<ReportInfo> reports = proxy.getListener().getReports();
        assertEquals(1, reports.size());

        final RamlViolations requestViolations = reports.get(0).getReport().getRequestViolations();
        assertEquals(1, requestViolations.size());
        assertEquals("Query parameter 'param' on action(GET /data) is not defined", requestViolations.iterator().next());

        final RamlViolations responseViolations = reports.get(0).getReport().getResponseViolations();
        assertEquals(1, responseViolations.size());
        assertThat(responseViolations.iterator().next(), startsWith(
                "Body does not match schema for action(GET /data) response(200) mime-type('application/json')\n" +
                        "Content: illegal json\n"));
    }

    @Test
    public void httpsTest() throws Exception {
        final RamlProxy<SavingRamlTesterListener> proxy = RamlProxy.create(new SavingRamlTesterListener(), new OptionContainer(proxyPort(),
                "https://api.github.com", "file://src/test/resources/guru/nidi/ramlproxy/github-meta.raml", null));
        proxy.start();

        final HttpGet get = new HttpGet(url("meta"));
        final CloseableHttpResponse response = client.execute(get);
        final String res = EntityUtils.toString(response.getEntity());

        proxy.stop();

        final List<ReportInfo> reports = proxy.getListener().getReports();
        assertEquals(1, reports.size());
        assertTrue(reports.get(0).getReport().getRequestViolations().isEmpty());
        assertTrue(reports.get(0).getReport().getResponseViolations().isEmpty());
    }

    private static class TestServlet extends HttpServlet {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            resp.setContentType("application/json");
            final PrintWriter out = resp.getWriter();
            out.write(req.getParameter("param") == null ? "42" : "illegal json");
            out.flush();
        }
    }

    @Override
    protected void init(Context ctx) {
        Tomcat.addServlet(ctx, "app", new TestServlet());
        ctx.addServletMapping("/*", "app");
    }
}
