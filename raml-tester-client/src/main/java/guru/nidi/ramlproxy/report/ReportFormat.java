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
package guru.nidi.ramlproxy.report;

import com.fasterxml.jackson.databind.ObjectMapper;
import guru.nidi.ramlproxy.data.UsageData;
import guru.nidi.ramlproxy.data.ViolationData;
import guru.nidi.ramltester.core.RamlReport;
import guru.nidi.ramltester.core.Usage;
import guru.nidi.ramltester.model.RamlMessage;
import guru.nidi.ramltester.servlet.ServletRamlRequest;
import guru.nidi.ramltester.servlet.ServletRamlResponse;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Set;

public enum ReportFormat {
    TEXT("log") {
        @Override
        public String formatUsage(Usage usage) throws IOException {
            final StringBuilder s = new StringBuilder();
            addIfNonempty(s, "Unused resources       ", usage.getUnusedResources());
            addIfNonempty(s, "Unused actions         ", usage.getUnusedActions());
            addIfNonempty(s, "Unused form parameters ", usage.getUnusedFormParameters());
            addIfNonempty(s, "Unused query parameters", usage.getUnusedQueryParameters());
            addIfNonempty(s, "Unused request headers ", usage.getUnusedRequestHeaders());
            addIfNonempty(s, "Unused response headers", usage.getUnusedResponseHeaders());
            addIfNonempty(s, "Unused response codes  ", usage.getUnusedResponseCodes());
            return s.toString();
        }

        @Override
        public String formatViolations(long id, RamlReport report, ServletRamlRequest request, ServletRamlResponse response) throws IOException {
            final ViolationData data = createViolationData(id, report, request, response);
            return "Request violations: " + data.getRequestViolations() + "\n\n" +
                    data.getRequest() + "\n" +
                    formatHeaders(data.getRequestHeaders()) + "\n" +
                    "\n\n\nResponse violations: " + data.getResponseViolations() + "\n\n" +
                    formatHeaders(data.getResponseHeaders()) + "\n" +
                    data.getResponse();
        }

        private String formatHeaders(Map<String, List<Object>> values) {
            final StringBuilder res = new StringBuilder();
            for (final Map.Entry<String, List<Object>> entry : values.entrySet()) {
                for (final Object value : entry.getValue()) {
                    res.append(entry.getKey()).append(": ").append(value).append('\n');
                }
            }
            return res.toString();
        }

    },
    JSON("json") {
        @Override
        public String formatUsage(Usage usage) throws IOException {
            return MapperHolder.MAPPER.writeValueAsString(createUsageData(usage));
        }

        @Override
        public String formatViolations(long id, RamlReport report, ServletRamlRequest request, ServletRamlResponse response) throws IOException {
            return MapperHolder.MAPPER.writeValueAsString(createViolationData(id, report, request, response));
        }
    };

    final String fileExtension;

    ReportFormat(String fileExtension) {
        this.fileExtension = fileExtension;
    }

    public abstract String formatUsage(Usage usage) throws IOException;

    public abstract String formatViolations(long id, RamlReport report, ServletRamlRequest request, ServletRamlResponse response) throws IOException;

    static String formatRequest(ServletRamlRequest request) {
        return request.getMethod() + " " + request.getRequestURL() +
                (request.getQueryString() == null ? "" : ("?" + request.getQueryString())) +
                " from " + request.getRemoteHost();
    }

    private static class MapperHolder {
        private final static ObjectMapper MAPPER = new ObjectMapper();
    }

    private static void addIfNonempty(StringBuilder base, String desc, Set<String> s) {
        if (!s.isEmpty()) {
            base.append(desc + ": " + s + "\n");
        }
    }

    private static String content(RamlMessage message, String encoding) throws UnsupportedEncodingException {
        return message.getContent() == null
                ? "No content"
                : new String(message.getContent(), StringUtils.defaultIfBlank(encoding, Charset.defaultCharset().name()));
    }

    public static UsageData createUsageData(Usage usage) {
        return new UsageData(usage.getUnusedActions(), usage.getUnusedResources(),
                usage.getUnusedRequestHeaders(), usage.getUnusedQueryParameters(),
                usage.getUnusedFormParameters(), usage.getUnusedResponseHeaders(),
                usage.getUnusedResponseCodes());
    }

    public static ViolationData createViolationData(long id, RamlReport report, ServletRamlRequest request, ServletRamlResponse response) throws UnsupportedEncodingException {
        return ViolationData.of(id,
                formatRequest(request), request.getHeaderValues().asMap(),
                report.getRequestViolations(),
                content(response, response.getCharacterEncoding()), response.getHeaderValues().asMap(),
                report.getResponseViolations());
    }
}
