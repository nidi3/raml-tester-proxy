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

import guru.nidi.ramltester.core.RamlViolations;
import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

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
        client = HttpClientBuilder.create().build();
    }

    @Test
    public void simpleOk() throws Exception {
        final HttpGet get = new HttpGet(url("data"));
        final CloseableHttpResponse response = client.execute(get);
        final String res = EntityUtils.toString(response.getEntity());
        stopProxy();
        assertEquals("42", res);
        final List<SavingRamlTesterListener.ReportInfo> reports = getRamlTesterListener().getReports();
        assertEquals(1, reports.size());
        assertTrue(reports.get(0).getReport().getRequestViolations().isEmpty());
        assertTrue(reports.get(0).getReport().getResponseViolations().isEmpty());
    }

    @Test
    public void simpleNok() throws Exception {
        final HttpGet get = new HttpGet(url("data?param=1"));
        final CloseableHttpResponse response = client.execute(get);
        final String res = EntityUtils.toString(response.getEntity());
        stopProxy();
        assertEquals("illegal json", res);
        final List<SavingRamlTesterListener.ReportInfo> reports = getRamlTesterListener().getReports();
        assertEquals(1, reports.size());
        final RamlViolations requestViolations = reports.get(0).getReport().getRequestViolations();
        assertEquals(1, requestViolations.size());
        assertEquals("Query parameter 'param' on action(GET /data) is not defined", requestViolations.iterator().next());
        final RamlViolations responseViolations = reports.get(0).getReport().getResponseViolations();
        assertEquals(1, responseViolations.size());
        assertThat(responseViolations.iterator().next(), CoreMatchers.startsWith(
                "Body does not match schema for action(GET /data) response(200) mime-type('application/json')\n" +
                        "Content: illegal json\n"));
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
