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
import guru.nidi.ramlproxy.core.RamlProxyServer;
import guru.nidi.ramlproxy.core.ServerOptions;
import guru.nidi.ramlproxy.report.ReportFormat;
import guru.nidi.ramlproxy.report.Reporter;
import org.apache.catalina.LifecycleException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.servlet.ServletException;
import java.io.*;
import java.util.List;
import java.util.Map;

import static guru.nidi.ramlproxy.CollectionUtils.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

/**
 *
 */
public class ReporterTest {
    private static TomcatServer tomcat;
    private HttpSender sender = new HttpSender(8091);

    @BeforeClass
    public static void init() throws ServletException, LifecycleException {
        tomcat = new TomcatServer(8081, new SimpleServlet());
    }

    @AfterClass
    public static void end() throws LifecycleException {
        tomcat.close();
    }

    @Test
    public void reporterText() throws Exception {
        final Reporter reporter = reporterTest(ReportFormat.TEXT);
        final File usageFile = reporter.usageFile("simple");
        assertTrue(usageFile.exists());
        //order of list has changed in jdk 1.8
        assertThat(read(usageFile), either(
                equalTo("" +
                        "Unused resources       : [/unused]\n" +
                        "Unused actions         : [GET /unused, POST /data]\n" +
                        "Unused form parameters : [a in POST /data (application/x-www-form-urlencoded)]\n" +
                        "Unused query parameters: [q in GET /data]\n" +
                        "Unused request headers : [head in GET /data]\n" +
                        "Unused response headers: [rh in GET /data -> 200]\n" +
                        "Unused response codes  : [201 in GET /data, 201 in POST /data]\n"))
                .or(equalTo("" +
                        "Unused resources       : [/unused]\n" +
                        "Unused actions         : [POST /data, GET /unused]\n" +
                        "Unused form parameters : [a in POST /data (application/x-www-form-urlencoded)]\n" +
                        "Unused query parameters: [q in GET /data]\n" +
                        "Unused request headers : [head in GET /data]\n" +
                        "Unused response headers: [rh in GET /data -> 200]\n" +
                        "Unused response codes  : [201 in POST /data, 201 in GET /data]\n")));
    }

    private String read(File f) throws IOException {
        String res = "";
        try (final BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(f)))) {
            while (in.ready()) {
                res += in.readLine() + "\n";
            }
        }
        return res;
    }

    @Test
    public void reporterJson() throws Exception {
        final Reporter reporter = reporterTest(ReportFormat.JSON);
        final ObjectMapper mapper = new ObjectMapper();
        assertUsageMap(map(
                "unusedRequestHeaders", list("head in GET /data"),
                "unusedFormParameters", list("a in POST /data (application/x-www-form-urlencoded)"),
                "unusedResponseHeaders", list("rh in GET /data -> 200"),
                "unusedResponseCodes", list("201 in GET /data", "201 in POST /data"),
                "unusedResources", list("/unused"),
                "unusedQueryParameters", list("q in GET /data"),
                "unusedActions", list("GET /unused", "POST /data")),
                mapper.readValue(reporter.usageFile("simple"), Map.class));

        final Map actual = mapper.readValue(reporter.violationsFile(1), Map.class);
        @SuppressWarnings("unchecked")
        final List<String> resVio = (List<String>) actual.get("responseViolations");
        assertThat(resVio.get(0), startsWith("Body does not match schema for action(GET /data) response(200) mime-type('application/json')\nContent: illegal json\n"));
        assertEquals(map("id", 1,
                "request", "GET " + sender.url("data?param=1 from 127.0.0.1"),
                "requestHeaders", map(
                        "Connection", list("keep-alive"),
                        "User-Agent", ((Map) actual.get("requestHeaders")).get("User-Agent"),
                        "Host", list("localhost:" + sender.getPort()),
                        "Accept-Encoding", list("gzip,deflate")),
                "requestViolations", list("Query parameter 'param' on action(GET /data) is not defined"),
                "response", "illegal json",
                "responseHeaders", map(
                        "Server", list("Apache-Coyote/1.1"),
                        "Date", ((Map) actual.get("responseHeaders")).get("Date"),
                        "Content-Type", list("application/json;charset=ISO-8859-1")),
                "responseViolations", resVio),
                actual);
    }

    private Reporter reporterTest(ReportFormat format) throws Exception {
        final Reporter reporter = new Reporter(new File(Ramls.clientDir("target")), format);
        try (final RamlProxyServer proxy = RamlProxy.startServerSync(new ServerOptions(sender.getPort(),
                tomcat.url(), Ramls.SIMPLE, "http://nidi.guru/raml/v1"), reporter)) {
            final String res = sender.contentOfGet("data?param=1");

            assertEquals("illegal json", res);
            return reporter;
        }
    }
}
