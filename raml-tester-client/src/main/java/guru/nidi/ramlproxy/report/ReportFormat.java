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
import guru.nidi.ramltester.core.RamlReport;
import guru.nidi.ramltester.model.RamlMessage;
import guru.nidi.ramltester.model.Values;
import guru.nidi.ramltester.servlet.ServletRamlRequest;
import guru.nidi.ramltester.servlet.ServletRamlResponse;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import static guru.nidi.ramlproxy.util.CollectionUtils.map;

/**
 *
 */
public enum ReportFormat {
    TEXT("log") {
        @Override
        public String formatUsage(String key, DescribedUsage describedUsage) throws IOException {
            return describedUsage.toString();
        }

        @Override
        public String formatViolations(long id, RamlReport report, ServletRamlRequest request, ServletRamlResponse response) throws IOException {
            return "Request violations: " + report.getRequestViolations() + "\n\n" +
                    formatRequest(request) + "\n" +
                    formatHeaders(request.getHeaderValues()) + "\n" +
                    content(request, request.getCharacterEncoding()) +
                    "\n\n\nResponse violations: " + report.getResponseViolations() + "\n\n" +
                    formatHeaders(response.getHeaderValues()) + "\n" +
                    content(response, response.getCharacterEncoding());
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

    },
    JSON("json") {
        private final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

        @Override
        public String formatUsage(String key, DescribedUsage describedUsage) throws IOException {
            return OBJECT_MAPPER.writeValueAsString(map(
                    "context", key,
                    "unused", describedUsage.asMap()));
        }

        @Override
        public String formatViolations(long id, RamlReport report, ServletRamlRequest request, ServletRamlResponse response) throws IOException {
            return OBJECT_MAPPER.writeValueAsString(map(
                    "id", id,
                    "request violations", report.getRequestViolations().asList(),
                    "request", formatRequest(request),
                    "request headers", request.getHeaderValues().asMap(),
                    "response violations", report.getResponseViolations().asList(),
                    "response", content(response, response.getCharacterEncoding()),
                    "response headers", response.getHeaderValues().asMap()));
        }

    };

    final String fileExtension;

    ReportFormat(String fileExtension) {
        this.fileExtension = fileExtension;
    }

    public abstract String formatUsage(String key, DescribedUsage describedUsage) throws IOException;

    public abstract String formatViolations(long id, RamlReport report, ServletRamlRequest request, ServletRamlResponse response) throws IOException;

    static String formatRequest(ServletRamlRequest request) {
        return request.getMethod() + " " + request.getRequestURL() +
                (request.getQueryString() == null ? "" : ("?" + request.getQueryString())) +
                " from " + request.getRemoteHost();

    }

    private static String content(RamlMessage message, String encoding) throws UnsupportedEncodingException {
        return message.getContent() == null
                ? "No content"
                : new String(message.getContent(), StringUtils.defaultIfBlank(encoding, Charset.defaultCharset().name()));
    }
}
