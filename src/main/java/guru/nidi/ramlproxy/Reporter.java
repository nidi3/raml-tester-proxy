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

import guru.nidi.ramltester.MultiReportAggregator;
import guru.nidi.ramltester.core.RamlReport;
import guru.nidi.ramltester.core.Usage;
import guru.nidi.ramltester.model.Values;
import guru.nidi.ramltester.servlet.ServletRamlRequest;
import guru.nidi.ramltester.servlet.ServletRamlResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 *
 */
public class Reporter {
    private static final byte[] NO_CONTENT = "No content".getBytes();

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final File saveDir;
    private final String startup = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").format(new Date());
    private final AtomicLong id = new AtomicLong();

    public Reporter(File saveDir) {
        this.saveDir = saveDir;
    }

    public void reportViolations(RamlReport report, ServletRamlRequest request, ServletRamlResponse response) {
        if (!report.isEmpty()) {
            final long idValue = id.incrementAndGet();
            logViolations(report, request, idValue);
            fileViolations(idValue, report, request, response);
        }
    }

    private void logViolations(RamlReport report, ServletRamlRequest request, long idValue) {
        log.error("<{}> {}\n           Request:  {}\n           Response: {}", idValue, formatRequest(request), report.getRequestViolations(), report.getResponseViolations());
    }

    public void reportUsage(MultiReportAggregator aggregator) {
        for (Map.Entry<String, Usage> entry : aggregator.usages()) {
            final Usage usage = entry.getValue();
            final String s = unused(usage.getUnusedResources(), "resources")
                    + unused(usage.getUnusedActions(), "actions")
                    + unused(usage.getUnusedRequestHeaders(), "request headers")
                    + unused(usage.getUnusedQueryParameters(), "query parameters")
                    + unused(usage.getUnusedFormParameters(), "form parameters")
                    + unused(usage.getUnusedResponseHeaders(), "response headers")
                    + unused(usage.getUnusedResponseCodes(), "response codes");
            logUsage(entry.getKey() + "\n" + s);
            fileUsage(entry.getKey(), s);
        }
    }

    private void fileUsage(String key, String s) {
        if (saveDir == null) {
            return;
        }
        try {
            final String filename = "raml-usage-" + startup + "--" + key.replace(" ", "-") + ".log";
            try (OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(new File(saveDir, filename)), "utf-8")) {
                out.write(s);
            }
        } catch (IOException e) {
            log.error("Problem writing error file", e);
        }
    }

    private void logUsage(String s) {
        log.error(s);
    }

    private String unused(Set<String> values, String desc) {
        if (values.isEmpty()) {
            return "";
        }
        String res = "  Unused " + desc + "\n";
        for (String value : new TreeSet<>(values)) {
            res += "    " + value + "\n";
        }
        return res;
    }

    private void fileViolations(long idValue, RamlReport report, ServletRamlRequest request, ServletRamlResponse response) {
        if (saveDir == null) {
            return;
        }
        try {
            final String filename = "raml-violation-" + startup + "--" + idValue + ".log";
            try (FileOutputStream out = new FileOutputStream(new File(saveDir, filename))) {
                out.write(("Request violations: " + report.getRequestViolations() + "\n\n").getBytes());
                out.write((formatRequest(request) + "\n").getBytes());
                out.write((formatHeaders(request.getHeaderValues()) + "\n").getBytes());
                out.write((request.getContent() == null ? NO_CONTENT : request.getContent()));
                out.write(("\n\n\nResponse violations: " + report.getResponseViolations() + "\n\n").getBytes());
                out.write((formatHeaders(response.getHeaderValues()) + "\n").getBytes());
                out.write((response.getContent() == null ? NO_CONTENT : response.getContent()));
            }
        } catch (IOException e) {
            log.error("Problem writing error file", e);
        }
    }

    private String formatRequest(ServletRamlRequest request) {
        return request.getMethod() + " " + request.getRequestURL() +
                (request.getQueryString() == null ? "" : ("?" + request.getQueryString())) +
                " from " + request.getRemoteHost();

    }

    private String formatHeaders(Values values) {
        String res = "";
        for (Map.Entry<String, List<Object>> entry : values) {
            for (Object value : entry.getValue()) {
                res += entry.getKey() + ": " + value + "\n";
            }
        }
        return res;
    }

}
