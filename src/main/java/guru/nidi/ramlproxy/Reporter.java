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

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 *
 */
public class Reporter {

    public static enum FileFormat {
        TEXT("log") {
            @Override
            String formatUsage(Reporter reporter, String key, Map<String, Set<String>> unuseds) throws IOException {
                return reporter.usageToString(unuseds);
            }

            @Override
            String formatViolations(Reporter reporter, long idValue, RamlReport report, ServletRamlRequest request, ServletRamlResponse response) throws IOException
            {
                return reporter.violationsToString(idValue, report, request, response);
            }
        },
        JSON("json") {
            @Override
            String formatUsage(Reporter reporter, String key, Map<String, Set<String>> unuseds) throws IOException {
                return reporter.usageToJson(key, unuseds);
            }

            @Override
            String formatViolations(Reporter reporter, long idValue, RamlReport report, ServletRamlRequest request, ServletRamlResponse response) throws IOException
            {
                return reporter.violationsToJson(idValue, report, request, response);
            }
        };

        private final String fileExtension;

        private FileFormat(String fileExtension) {
            this.fileExtension=fileExtension;
        }

        abstract String formatUsage(Reporter reporter, String key, Map<String, Set<String>> unuseds) throws IOException;
        abstract String formatViolations(Reporter reporter, long idValue, RamlReport report, ServletRamlRequest request, ServletRamlResponse response) throws IOException;
    };

    private static final String NO_CONTENT = "No content";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final File saveDir;
    private final FileFormat fileFormat;
    private final String startup = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").format(new Date());
    private final AtomicLong id = new AtomicLong();

    public Reporter(File saveDir, FileFormat fileFormat) {
        this.saveDir = saveDir;
        this.fileFormat = fileFormat;
        log.info("Reporting in " + fileFormat + " format into: " + saveDir);
    }

    public void reportViolations(RamlReport report, ServletRamlRequest request, ServletRamlResponse response) {
        if (!report.isEmpty()) {
            final long idValue = id.incrementAndGet();
            logViolations(report, request, idValue);
            fileViolations(idValue, report, request, response);
        }
    }

    public void reportUsage(MultiReportAggregator aggregator) {
        for (Map.Entry<String, Usage> entry : aggregator.usages()) {
            final Usage usage = entry.getValue();
            final Map<String, Set<String>> unuseds = new HashMap<>();
            unused(usage.getUnusedResources(), "resources", unuseds);
            unused(usage.getUnusedActions(), "actions", unuseds);
            unused(usage.getUnusedRequestHeaders(), "request headers", unuseds);
            unused(usage.getUnusedQueryParameters(), "query parameters", unuseds);
            unused(usage.getUnusedFormParameters(), "form parameters", unuseds);
            unused(usage.getUnusedResponseHeaders(), "response headers", unuseds);
            unused(usage.getUnusedResponseCodes(), "response codes", unuseds);
            logUsage(entry.getKey(), unuseds);
            fileUsage(entry.getKey(), unuseds);
        }
    }

    private void fileUsage(String key, Map<String, Set<String>> unuseds) {
        if (saveDir == null) {
            return;
        }
        try {
            final String filename = "raml-usage-" + startup + "--" + key.replace(" ", "-") + "." + fileFormat.fileExtension;
            try (OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(new File(saveDir, filename)), "utf-8")) {
                out.write(fileFormat.formatUsage(this, key, unuseds));
            }
        } catch (IOException e) {
            log.error("Problem writing error file", e);
        }
    }

    private void logUsage(String key, Map<String, Set<String>> unuseds) {
        log.error(key + "\n" + usageToString(unuseds));
    }

    private String usageToString(Map<String, Set<String>> unuseds) {
        final StringBuilder sb = new StringBuilder();
        for (final Map.Entry<String, Set<String>> unused : unuseds.entrySet()) {
            sb.append("  Unused ").append(unused.getKey()).append("\n");
            for (final String value : unused.getValue()) {
                sb.append("    ").append(value).append("\n");
            }
        }
        return sb.toString();
    }

    protected String usageToJson(String key, Map<String, Set<String>> unuseds) throws IOException {
        Map<String,Object> json = new HashMap<>();
        json.put("context", key);
        json.put("unused", unuseds);
        return OBJECT_MAPPER.writeValueAsString(json);
    }

    private void unused(Set<String> values, String desc, Map<String, Set<String>> acc) {
        if (!values.isEmpty()) {
            acc.put(desc, values);
        }
    }

    private void logViolations(RamlReport report, ServletRamlRequest request, long idValue) {
        log.error("<{}> {}\n           Request:  {}\n           Response: {}", idValue, formatRequest(request), report.getRequestViolations(), report.getResponseViolations());
    }

    private void fileViolations(long idValue, RamlReport report, ServletRamlRequest request, ServletRamlResponse response) {
        if (saveDir == null) {
            return;
        }
        try {
            final String filename = "raml-violation-" + startup + "--" + idValue + "." + fileFormat.fileExtension;
            try (OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(new File(saveDir, filename)))) {
                out.write(fileFormat.formatViolations(this, idValue, report, request, response));
            }
        } catch (IOException e) {
            log.error("Problem writing error file", e);
        }
    }

    private String violationsToString(long idValue, RamlReport report, ServletRamlRequest request, ServletRamlResponse response) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("Request violations: ").append(report.getRequestViolations()).append("\n\n");
        sb.append(formatRequest(request)).append("\n");
        sb.append(formatHeaders(request.getHeaderValues())).append("\n");
        sb.append(request.getContent() == null ? NO_CONTENT : new String(request.getContent(), encoding(request)));
        sb.append("\n\n\nResponse violations: ").append(report.getResponseViolations()).append("\n\n");
        sb.append(formatHeaders(response.getHeaderValues())).append("\n");
        sb.append(response.getContent() == null ? NO_CONTENT : new String(response.getContent(), encoding(response)));
        return sb.toString();
    }

    private String encoding(ServletRamlRequest request) {
        return StringUtils.defaultIfBlank(request.getCharacterEncoding(), Charset.defaultCharset().name());
    }

    private String encoding(ServletRamlResponse response) {
        return StringUtils.defaultIfBlank(response.getCharacterEncoding(), Charset.defaultCharset().name());
    }

    private String violationsToJson(long idValue, RamlReport report, ServletRamlRequest request, ServletRamlResponse response) throws IOException {
        Map<String,Object> json = new HashMap<>();
        json.put("id", idValue);
        json.put("request violations", Lists.newArrayList(report.getRequestViolations()));
        json.put("request", formatRequest(request));
        json.put("request headers", request.getHeaderValues());
        json.put("response violations", Lists.newArrayList(report.getResponseViolations()));
        json.put("response", (response.getContent() == null ? NO_CONTENT : response.getContent()));
        json.put("response headers", response.getHeaderValues());
        return OBJECT_MAPPER.writeValueAsString(json);
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
