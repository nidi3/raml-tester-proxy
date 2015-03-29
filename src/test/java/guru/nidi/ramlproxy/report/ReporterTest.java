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
package guru.nidi.ramlproxy.report;

import com.fasterxml.jackson.databind.ObjectMapper;
import guru.nidi.ramlproxy.*;
import org.apache.catalina.LifecycleException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.servlet.ServletException;
import java.io.File;
import java.util.List;
import java.util.Map;

import static guru.nidi.ramlproxy.util.CollectionUtils.list;
import static guru.nidi.ramlproxy.util.CollectionUtils.map;
import static org.hamcrest.CoreMatchers.startsWith;
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
                                "response codes", list("201 in GET /data", "201 in POST /data"),
                                "resources", list("/other", "/super/sub"),
                                "query parameters", list("q in GET /data"),
                                "actions", list("POST /data"))),
                mapper.readValue(reporter.usageFile("simple"), Map.class));

        final Map actual = mapper.readValue(reporter.violationsFile(1), Map.class);
        final List<String> resVio = (List<String>) actual.get("response violations");
        assertThat(resVio.get(0), startsWith("Body does not match schema for action(GET /data) response(200) mime-type('application/json')\nContent: illegal json\n"));
        assertEquals(map("id", 1,
                        "request", "GET " + sender.url("data?param=1 from 127.0.0.1"),
                        "request headers", map(
                                "Connection", list("keep-alive"),
                                "User-Agent", ((Map) actual.get("request headers")).get("User-Agent"),
                                "Host", list("localhost:" + sender.getPort()),
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

    private Reporter reporterTest(ReportFormat format) throws Exception {
        final Reporter reporter = new Reporter(new File("target"), format);
        try (final RamlProxy proxy = new RamlProxy(reporter, new ServerOptions(sender.getPort(),
                tomcat.url(), Ramls.SIMPLE, "http://nidi.guru/raml/v1"))) {
            final String res = sender.contentOfGet("data?param=1");

            assertEquals("illegal json", res);
            return reporter;
        }
    }
}
