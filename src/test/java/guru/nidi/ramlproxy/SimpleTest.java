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

import guru.nidi.ramlproxy.SavingRamlTesterListener.ReportInfo;
import guru.nidi.ramltester.core.RamlViolations;
import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.*;

/**
 *
 */
public class SimpleTest extends AbstractServerTest {
    private static final String GITHUB_RAML = "file://src/test/resources/guru/nidi/ramlproxy/github-meta.raml";
    private static final String SIMPLE_RAML = "file://src/test/resources/guru/nidi/ramlproxy/simple.raml";

    @Override
    protected int serverPort() {
        return 8080;
    }

    @Override
    protected int proxyPort() {
        return 8090;
    }

    @Override
    protected void init(Context ctx) {
        Tomcat.addServlet(ctx, "app", new SimpleServlet());
        ctx.addServletMapping("/*", "app");
    }

    @Test
    public void simpleOk() throws Exception {
        final OptionContainer options = new OptionContainer(proxyPort(), "http://localhost:" + serverPort(), SIMPLE_RAML, "http://nidi.guru/raml/v1");
        final RamlProxy<SavingRamlTesterListener> proxy = RamlProxy.create(new SavingRamlTesterListener(), options);
        final String res = executeGet(proxy, "data");

        assertEquals("42", res);

        final List<ReportInfo> reports = proxy.getListener().getReports();
        assertEquals(1, reports.size());

        assertTrue(reports.get(0).getReport().getRequestViolations().isEmpty());
        assertTrue(reports.get(0).getReport().getResponseViolations().isEmpty());
    }

    @Test
    public void simpleNok() throws Exception {
        final OptionContainer options = new OptionContainer(proxyPort(), "http://localhost:" + serverPort(), SIMPLE_RAML, "http://nidi.guru/raml/v1");
        final RamlProxy<SavingRamlTesterListener> proxy = RamlProxy.create(new SavingRamlTesterListener(), options);
        final String res = executeGet(proxy, "data?param=1");

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
        final OptionContainer options = new OptionContainer(proxyPort(), "https://api.github.com", GITHUB_RAML, null);
        final RamlProxy<SavingRamlTesterListener> proxy = RamlProxy.create(new SavingRamlTesterListener(), options);
        executeGet(proxy, "meta");

        final List<ReportInfo> reports = proxy.getListener().getReports();
        assertEquals(1, reports.size());
        assertTrue(reports.get(0).getReport().getRequestViolations().isEmpty());
        for (final String resViol : reports.get(0).getReport().getResponseViolations()) {
            assertThat(resViol, startsWith("Header 'X-"));
        }
    }

    @Test
    public void testIgnoreX() throws Exception {
        final OptionContainer options = new OptionContainer(proxyPort(), "https://api.github.com", GITHUB_RAML, null, null, null, true);
        final RamlProxy<SavingRamlTesterListener> proxy = RamlProxy.create(new SavingRamlTesterListener(), options);
        executeGet(proxy, "meta");

        final List<ReportInfo> reports = proxy.getListener().getReports();
        assertEquals(1, reports.size());
        assertTrue(reports.get(0).getReport().getRequestViolations().isEmpty());
        assertTrue(reports.get(0).getReport().getResponseViolations().isEmpty());
    }


}
