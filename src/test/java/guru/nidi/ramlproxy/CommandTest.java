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
import guru.nidi.ramltester.SimpleReportAggregator;
import guru.nidi.ramltester.core.RamlReport;
import guru.nidi.ramltester.core.Usage;
import guru.nidi.ramltester.core.UsageBuilder;
import guru.nidi.ramltester.junit.ExpectedUsage;
import org.apache.http.HttpResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static guru.nidi.ramlproxy.CollectionUtils.list;
import static guru.nidi.ramlproxy.CollectionUtils.map;
import static guru.nidi.ramlproxy.Command.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;

/**
 * Client ----> Proxy ----> Mock ----> Filesystem
 * Test commands on Mock
 * Test if commands satisfy RAML using Proxy
 */
public class CommandTest {
    private HttpSender mockSender = new HttpSender(8091);
    private HttpSender proxySender = new HttpSender(8090);
    private static SimpleReportAggregator aggregator = new UnclearableReportAggregator();
    private static RamlProxy mock, proxy;

    @ClassRule
    public static ExpectedUsage expectedUsage = new ExpectedUsage(aggregator);

    @Before
    public void init() throws Exception {
        proxySender.setIgnoreCommands(true);
        try {
            mockSender.contentOfGet("/@@@proxy/stop");
        } catch (Exception e) {
        }
        mock = RamlProxy.create(new ReportSaver(), new OptionContainer(
                mockSender.getPort(), Ramls.MOCK_DIR, Ramls.SIMPLE, "http://nidi.guru/raml", new File("target"), null, true));
        proxy = RamlProxy.create(new ReportSaver(aggregator), new OptionContainer(
                proxySender.getPort(), mockSender.url(), Ramls.COMMAND, null));
    }

    @After
    public void end() throws Exception {
        mock.close();
        proxy.close();
        for (ReportSaver.ReportInfo info : proxy.getSaver().getReports()) {
            final RamlReport report = info.getReport();
            assertTrue(report.getRequestViolations() + "\n" + report.getResponseViolations(), report.isEmpty());
        }
    }

    @Test
    public void reports() throws Exception {
        mockSender.get("v1/data?q=1");
        Thread.sleep(10);

        final HttpResponse res = mockSender.get(mock.commandUrl(REPORTS));
        final List actual = new ObjectMapper().readValue(proxySender.content(res), List.class);
        assertEquals(list(map(
                        "id", 0,
                        "request violations", list(),
                        "request", "GET " + mockSender.url("v1/data") + "?q=1 from 127.0.0.1",
                        "request headers", map(
                                "Connection", list("keep-alive"),
                                "User-Agent", ((Map) ((Map) actual.get(0)).get("request headers")).get("User-Agent"),
                                "Host", list("localhost:" + mockSender.getPort()),
                                "Accept-Encoding", list("gzip,deflate")),
                        "response violations", list("Response(202) is not defined on action(GET /data)"),
                        "response", "42",
                        "response headers", map("X-meta", list("get!")))),
                actual);
    }

    @Test
    public void stop() throws Exception {
        proxySender.get(proxy.commandUrl(STOP));
        Thread.sleep(200);
        assertTrue(mock.isStopped());
    }

    @Test
    public void usage() throws Exception {
        mockSender.contentOfGet("v1/data?q=1");
        mockSender.contentOfGet("v1/other");
        Thread.sleep(10);

        final HttpResponse res = proxySender.get(proxy.commandUrl(USAGE));
        final List actual = new ObjectMapper().readValue(proxySender.content(res), List.class);
        assertEquals(list(map(
                        "context", "simple",
                        "unused", map(
                                "request headers", list("head in GET /data"),
                                "form parameters", list("a in POST /data (application/x-www-form-urlencoded)"),
                                "resources", list("/super/sub"),
                                "actions", list("POST /data"),
                                "response codes", list("200 in GET /data", "201 in GET /data", "201 in POST /data"),
                                "response headers", list("rh in GET /data -> 200")
                        ))),
                actual);
    }

    @Test
    public void clearUsage() throws Exception {
        mockSender.contentOfGet("v1/data?q=1");
        mockSender.contentOfGet("v1/other");
        Thread.sleep(10);

        proxySender.get(proxy.commandUrl(PING) + "?clear-usage=true");
        final HttpResponse res = proxySender.get(proxy.commandUrl(USAGE));
        final List actual = new ObjectMapper().readValue(proxySender.content(res), List.class);
        assertTrue(actual.isEmpty());
    }
    @Test
    public void reload() throws Exception {
        mockSender.get("meta");
        Thread.sleep(10);

        final HttpResponse res = proxySender.get(proxy.commandUrl(REPORTS)+"?clear-reports=true");
        final List actual = new ObjectMapper().readValue(proxySender.content(res), List.class);
        assertEquals(1, actual.size());

        assertThat(proxySender.contentOfGet(proxy.commandUrl(RELOAD)), equalTo("RAML reloaded"));
        final HttpResponse res2 = proxySender.get(proxy.commandUrl(REPORTS));
        final List actual2 = new ObjectMapper().readValue(proxySender.content(res2), List.class);
        assertEquals(0, actual2.size());
    }

    @Test
    public void options() throws Exception {
        final String optString = "-p" + mockSender.getPort() + " -m" + Ramls.MOCK_DIR + " -i -r" + Ramls.SIMPLE + " -bhttp://nidi.guru/raml -starget";
        final HttpResponse response = proxySender.post(proxy.commandUrl(OPTIONS), optString);
        assertEquals("same", proxySender.content(response));

        final String optString2 = "-p" + mockSender.getPort() + " -thttps://api.github.com -r" + Ramls.SIMPLE;
        final HttpResponse response2 = proxySender.post(proxy.commandUrl(OPTIONS), optString2);
        assertEquals("different", proxySender.content(response2));
    }

    @Test
    public void ping() throws Exception {
        assertEquals(proxySender.contentOfGet(proxy.commandUrl(PING)), "Pong");
    }

    private static class UnclearableReportAggregator extends SimpleReportAggregator {
        private List<RamlReport> unclearableReports = new ArrayList<>();

        @Override
        public RamlReport addReport(RamlReport report) {
            unclearableReports.add(report);
            return super.addReport(report);
        }

        @Override
        public Usage getUsage() {
            return UsageBuilder.usage(getRaml(), unclearableReports);
        }
    }

}
