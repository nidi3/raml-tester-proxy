/*
 * Copyright © 2014 Stefan Niederhauser (nidin@gmx.ch)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package guru.nidi.ramlproxy;

import guru.nidi.ramlproxy.core.RamlProxyServer;
import guru.nidi.ramlproxy.core.ServerOptions;
import guru.nidi.ramlproxy.report.ReportSaver;
import guru.nidi.ramlproxy.report.ReportSaver.ReportInfo;
import guru.nidi.ramltester.core.RamlViolationMessage;
import guru.nidi.ramltester.core.RamlViolations;
import org.apache.catalina.LifecycleException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.servlet.ServletException;
import java.util.List;

import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.*;

public class ProxyTest {
    private static TomcatServer tomcat;
    private HttpSender sender = new HttpSender(8090);

    @BeforeClass
    public static void start() throws ServletException, LifecycleException {
        tomcat = new TomcatServer(8080, new SimpleServlet());
    }

    @AfterClass
    public static void end() throws LifecycleException {
        tomcat.close();
    }

    @Test
    public void simpleOk() throws Exception {
        final ServerOptions options = new ServerOptions(sender.getPort(), tomcat.url(), Ramls.SIMPLE, "http://nidi.guru/raml/v1");
        try (final RamlProxyServer proxy = RamlProxy.startServerSync(options, new ReportSaver())) {
            final String res = sender.contentOfGet("data");

            assertEquals("42", res);

            final List<ReportInfo> reports = proxy.getSaver().getReports("simple");
            assertEquals(1, reports.size());

            assertTrue(reports.get(0).getReport().getRequestViolations().isEmpty());
            assertTrue(reports.get(0).getReport().getResponseViolations().isEmpty());
        }
    }

    @Test
    public void simpleNok() throws Exception {
        final ServerOptions options = new ServerOptions(sender.getPort(), tomcat.url(), Ramls.SIMPLE, "http://nidi.guru/raml/v1");
        try (final RamlProxyServer proxy = RamlProxy.startServerSync(options, new ReportSaver())) {
            final String res = sender.contentOfGet("data?param=1");

            assertEquals("illegal json", res);

            final List<ReportInfo> reports = proxy.getSaver().getReports("simple");
            assertEquals(1, reports.size());

            final RamlViolations requestViolations = reports.get(0).getReport().getRequestViolations();
            assertEquals(1, requestViolations.size());
            assertEquals("Query parameter 'param' on action(GET /data) is not defined", requestViolations.iterator().next().getMessage());

            final RamlViolations responseViolations = reports.get(0).getReport().getResponseViolations();
            assertEquals(1, responseViolations.size());
            assertThat(responseViolations.iterator().next().getMessage(), startsWith(
                    "Body does not match schema for action(GET /data) response(200) mime-type('application/json')\n" +
                            "Content: illegal json\n"));
        }
    }

    @Test
    public void httpsTest() throws Exception {
        final ServerOptions options = new ServerOptions(sender.getPort(), "https://api.github.com", Ramls.GITHUB, null);
        try (final RamlProxyServer proxy = RamlProxy.startServerSync(options, new ReportSaver())) {
            sender.contentOfGet("meta");

            final List<ReportInfo> reports = proxy.getSaver().getReports("github-meta");
            assertEquals(1, reports.size());
            assertTrue(reports.get(0).getReport().getRequestViolations().isEmpty());
            for (final RamlViolationMessage resViol : reports.get(0).getReport().getResponseViolations()) {
                assertThat(resViol.getMessage(), startsWith("Header 'X-"));
            }
        }
    }

    @Test
    public void testIgnoreX() throws Exception {
        final ServerOptions options = new ServerOptions(sender.getPort(), "https://api.github.com", Ramls.GITHUB, null, null, null, true);
        try (final RamlProxyServer proxy = RamlProxy.startServerSync(options, new ReportSaver())) {
            sender.contentOfGet("meta");

            final List<ReportInfo> reports = proxy.getSaver().getReports("github-meta");
            assertEquals(1, reports.size());
            assertTrue(reports.get(0).getReport().getRequestViolations().isEmpty());
            assertTrue(reports.get(0).getReport().getResponseViolations().isEmpty());
        }
    }


}
