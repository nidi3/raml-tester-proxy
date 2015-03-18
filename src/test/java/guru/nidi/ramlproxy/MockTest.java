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

import guru.nidi.ramlproxy.ReportSaver.ReportInfo;
import guru.nidi.ramltester.core.RamlReport;
import org.apache.http.HttpResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Iterator;
import java.util.List;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.*;

/**
 *
 */
public class MockTest {
    private static final String MOCK_DIR = "src/test/resources/guru/nidi/ramlproxy";
    private HttpSender sender = new HttpSender(8090);
    private RamlProxy proxy;

    @Before
    public void init() throws Exception {
        final OptionContainer options = new OptionContainer(sender.getPort(), MOCK_DIR, Ramls.SIMPLE, "http://nidi.guru/raml", null, null, true);
        proxy = RamlProxy.create(new ReportSaver(), options);
    }

    @After
    public void stop() throws Exception {
        proxy.close();
    }

    @Test
    public void simpleOk() throws Exception {
        final HttpResponse res = sender.get("v1/data");
        Thread.sleep(10);
        assertEquals("42", sender.content(res));
        assertEquals(202, res.getStatusLine().getStatusCode());
        assertEquals("get!", res.getFirstHeader("X-meta").getValue());

        final RamlReport report = assertOneReport();
        final Iterator<String> iter = report.getResponseViolations().iterator();
        assertEquals("Response(202) is not defined on action(GET /data)", iter.next());
        assertTrue(report.getRequestViolations().isEmpty());
    }

    @Test
    public void multipleFiles() throws Exception {
        final HttpResponse res = sender.get("v1/multi");
        Thread.sleep(10);

        assertEquals(404, res.getStatusLine().getStatusCode());
        final String content = sender.content(res);
        assertThat(content, containsString("No or multiple file 'multi' found in directory"));
        assertThat(content, containsString("src/test/resources/guru/nidi/ramlproxy/v1"));

        final RamlReport report = assertOneReport();
        final Iterator<String> iter = report.getRequestViolations().iterator();
        assertEquals("Resource '/multi' is not defined", iter.next());
        assertTrue(report.getResponseViolations().isEmpty());
    }

    @Test
    public void noFile() throws Exception {
        final HttpResponse res = sender.get("v1/notExisting");
        Thread.sleep(10);

        assertEquals(404, res.getStatusLine().getStatusCode());
        final String content = sender.content(res);
        assertThat(content, containsString("No or multiple file 'notExisting' found in directory"));
        assertThat(content, containsString("src/test/resources/guru/nidi/ramlproxy/v1"));

        final RamlReport report = assertOneReport();
        final Iterator<String> iter = report.getRequestViolations().iterator();
        assertEquals("Resource '/notExisting' is not defined", iter.next());
        assertTrue(report.getResponseViolations().isEmpty());
    }

    @Test
    public void withMethod() throws Exception {
        final HttpResponse res = sender.post("v1/data",null);
        Thread.sleep(10);

        assertEquals("666", sender.content(res));
        assertEquals(201, res.getStatusLine().getStatusCode());
        assertEquals("yes!", res.getFirstHeader("X-meta").getValue());
        assertEquals("5", res.getLastHeader("X-meta").getValue());

        final RamlReport report = assertOneReport();
        assertTrue(report.getRequestViolations().isEmpty());
        assertTrue(report.getResponseViolations().isEmpty());
    }

    @Test
    public void nested() throws Exception {
        final HttpResponse res = sender.post("v1/super/sub",null);
        Thread.sleep(10);

        assertEquals("163", sender.content(res));
        assertEquals("true", res.getFirstHeader("X-meta").getValue());

        final RamlReport report = assertOneReport();
        final Iterator<String> iter = report.getRequestViolations().iterator();
        assertEquals("Action POST is not defined on resource(/super/sub)", iter.next());
        assertTrue(report.getResponseViolations().isEmpty());
    }

    private RamlReport assertOneReport() {
        final List<ReportInfo> reports = proxy.getSaver().getReports();
        assertEquals(1, reports.size());
        return reports.get(0).getReport();
    }
}
