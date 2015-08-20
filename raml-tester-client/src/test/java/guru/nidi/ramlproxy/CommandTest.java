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
import guru.nidi.ramlproxy.report.*;
import guru.nidi.ramltester.SimpleReportAggregator;
import guru.nidi.ramltester.core.RamlReport;
import guru.nidi.ramltester.core.Usage;
import guru.nidi.ramltester.core.UsageBuilder;
import guru.nidi.ramltester.core.UsageItem;
import guru.nidi.ramltester.junit.ExpectedUsage;
import org.apache.http.HttpResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static guru.nidi.ramlproxy.CollectionUtils.list;
import static guru.nidi.ramlproxy.CollectionUtils.map;
import static guru.nidi.ramlproxy.core.Command.*;
import static guru.nidi.ramlproxy.core.CommandSender.content;
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
    private static RamlProxyServer mock, proxy;

    @ClassRule
    public static ExpectedUsage expectedUsage = new ExpectedUsage(aggregator,
            UsageItem.ACTION, UsageItem.FORM_PARAMETER, UsageItem.REQUEST_HEADER, UsageItem.RESOURCE, UsageItem.RESPONSE_CODE, UsageItem.RESPONSE_HEADER);

    @Before
    public void init() throws Exception {
        proxySender.setIgnoreCommands(true);
        try {
            mockSender.contentOfGet("/@@@proxy/stop");
        } catch (Exception e) {
            //ignore
        }
        mock = RamlProxy.startServerSync(
                new ServerOptions(mockSender.getPort(), Ramls.MOCK_DIR, Ramls.SIMPLE, "http://nidi.guru/raml", new File("target"), null, true),
                new ReportSaver());
        proxy = RamlProxy.startServerSync(
                new ServerOptions(proxySender.getPort(), mockSender.host(), Ramls.COMMAND, null),
                new ReportSaver(aggregator));
    }

    @After
    public void end() throws Exception {
        try {
            proxySender.setIgnoreCommands(false);
            final ViolationDatas violations = proxySender.send(REPORTS);
            assertEquals(1, violations.size());
            for (final ViolationData violation : violations.get("raml-proxy")) {
                assertTrue(violation.getRequestViolations().isEmpty());
                assertTrue(violation.getResponseViolations().isEmpty());
            }

            final UsageDatas usages = proxySender.send(USAGE);
            assertEquals(1, usages.size());
            assertNotNull(usages.get("raml-proxy"));

            for (ReportSaver.ReportInfo info : proxy.getSaver().getReports("raml-proxy")) {
                final RamlReport report = info.getReport();
                assertTrue(report.getRequestViolations() + "\n" + report.getResponseViolations(), report.isEmpty());
            }
        } finally {
            mock.close();
            proxy.close();
        }
    }

    @Test
    @SuppressWarnings({"AssertEqualsBetweenInconvertibleTypes", "unchecked"})
    public void reports() throws Exception {
        mockSender.get("v1/data?q=1");
        Thread.sleep(10);

        final HttpResponse res = proxySender.get(REPORTS);
        final String content = content(res);
        final Map<String, List<Map<String, Object>>> resAsMap = mapped(content, Map.class);
        assertEquals(map("simple", list(map(
                        "id", 0,
                        "requestViolations", list(),
                        "request", "GET " + mockSender.url("v1/data") + "?q=1 from 127.0.0.1",
                        "requestHeaders", map(
                                "Connection", list("keep-alive"),
                                "User-Agent", ((Map) resAsMap.get("simple").get(0).get("requestHeaders")).get("User-Agent"),
                                "Host", list("localhost:" + mockSender.getPort()),
                                "Accept-Encoding", list("gzip,deflate")),
                        "responseViolations", list("Response(202) is not defined on action(GET /data)"),
                        "response", "42",
                        "responseHeaders", map("X-meta", list("get!"))))),
                resAsMap);

        final ViolationDatas resAsData = mapped(content, ViolationDatas.class);
        final ViolationDatas datas = new ViolationDatas();
        datas.put("simple", list(new ViolationData(
                0L,
                "GET " + mockSender.url("v1/data") + "?q=1 from 127.0.0.1",
                map(
                        "Connection", list("keep-alive"),
                        "User-Agent", resAsData.get("simple").get(0).getRequestHeaders().get("User-Agent"),
                        "Host", list("localhost:" + mockSender.getPort()),
                        "Accept-Encoding", list("gzip,deflate")),
                list(),
                "42",
                map("X-meta", list("get!")),
                list("Response(202) is not defined on action(GET /data)"))));

        assertEquals(datas, resAsData);
    }

    @Test
    public void stop() throws Exception {
        proxySender.send(STOP);
        Thread.sleep(200);
        assertTrue(mock.isStopped());
    }

    @Test
    @SuppressWarnings("AssertEqualsBetweenInconvertibleTypes")
    public void usage() throws Exception {
        mockSender.contentOfGet("v1/data?q=1");
        mockSender.contentOfGet("v1/other");
        Thread.sleep(10);

        final HttpResponse res = proxySender.get(USAGE);
        final String content = content(res);
        final Map<String, Object> resAsMap = mapped(content, Map.class);
        assertEquals(map("simple", map(
                        "unusedQueryParameters", list(),
                        "unusedRequestHeaders", list("head in GET /data"),
                        "unusedFormParameters", list("a in POST /data (application/x-www-form-urlencoded)"),
                        "unusedResources", list("/super/sub"),
                        "unusedActions", list("POST /data"),
                        "unusedResponseCodes", list("200 in GET /data", "201 in GET /data", "201 in POST /data"),
                        "unusedResponseHeaders", list("rh in GET /data -> 200")
                )),
                resAsMap);

        final UsageDatas resAsUsage = mapped(content, UsageDatas.class);
        assertEquals(1, resAsUsage.size());

        final UsageData simple = resAsUsage.get("simple");

        assertEquals(list("POST /data"), simple.getUnusedActions());
        assertEquals(list("a in POST /data (application/x-www-form-urlencoded)"), simple.getUnusedFormParameters());
        assertEquals(list(), simple.getUnusedQueryParameters());
        assertEquals(list("head in GET /data"), simple.getUnusedRequestHeaders());
        assertEquals(list("/super/sub"), simple.getUnusedResources());
        assertEquals(list("200 in GET /data", "201 in GET /data", "201 in POST /data"), simple.getUnusedResponseCodes());
        assertEquals(list("rh in GET /data -> 200"), simple.getUnusedResponseHeaders());
    }

    @Test
    public void clearUsageQuery() throws Exception {
        mockSender.contentOfGet("v1/data?q=1");
        mockSender.contentOfGet("v1/other");
        Thread.sleep(10);

        proxySender.get(PING, "clear-usage=true");
        final HttpResponse res = proxySender.get(USAGE);
        final Map<String, Object> actual = mapped(content(res), Map.class);
        assertTrue(actual.isEmpty());
    }

    @Test
    public void clearUsageUrl() throws Exception {
        mockSender.contentOfGet("v1/data?q=1");
        mockSender.contentOfGet("v1/other");
        Thread.sleep(10);

        proxySender.send(CLEAR_USAGE);
        final HttpResponse res = proxySender.get(USAGE);
        final Map<String, Object> actual = mapped(content(res), Map.class);
        assertTrue(actual.isEmpty());
    }

    @Test
    public void clearReportsUrl() throws Exception {
        mockSender.contentOfGet("v1/data?q=1");
        mockSender.contentOfGet("v1/other");
        Thread.sleep(10);

        proxySender.send(CLEAR_REPORTS);
        final HttpResponse res = proxySender.get(REPORTS);
        final Map<String, Object> actual = mapped(content(res), Map.class);
        assertTrue(actual.isEmpty());
    }

    @Test
    public void reload() throws Exception {
        mockSender.get("meta");
        Thread.sleep(10);

        final HttpResponse res = proxySender.get(REPORTS, "clear-reports=true");
        final Map<String, List> actual = mapped(content(res), Map.class);
        assertEquals(1, actual.get("simple").size());

        proxySender.send(RELOAD);
        final HttpResponse res2 = proxySender.get(REPORTS);
        final Map<String, List> actual2 = mapped(content(res2), Map.class);
        assertEquals(0, actual2.size());
    }

    @Test
    public void ping() throws Exception {
        proxySender.send(PING);
    }

    @Test
    public void validation() throws Exception {
        final HttpResponse res = proxySender.get(VALIDATE);
        final String content = content(res);
        final ValidationData resAsData = mapped(content, ValidationData.class);
        assertEquals("simple", resAsData.getRamlTitle());
        assertEquals(resAsData.getValidationViolations().get(0), "Root definition has no description");
    }

    @SuppressWarnings("unchecked")
    private <T> T mapped(String source, Class<?> target) throws IOException {
        return (T) new ObjectMapper().readValue(source, target);
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
