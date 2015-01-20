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
import com.google.common.collect.Lists;
import guru.nidi.ramltester.core.RamlReport;
import guru.nidi.ramltester.model.Values;
import guru.nidi.ramltester.servlet.ServletRamlRequest;
import guru.nidi.ramltester.servlet.ServletRamlResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
public enum ReportFormat {
    TEXT("log") {
        @Override
        public String formatUsage(Reporter reporter, String key, DescribedUsage describedUsage) throws IOException {
            return describedUsage.toString();
        }

        @Override
        public String formatViolations(Reporter reporter, long idValue, RamlReport report, ServletRamlRequest request, ServletRamlResponse response) throws IOException {
            StringBuilder sb = new StringBuilder();
            sb.append("Request violations: ").append(report.getRequestViolations()).append("\n\n");
            sb.append(Reporter.formatRequest(request)).append("\n");
            sb.append(formatHeaders(request.getHeaderValues())).append("\n");
            sb.append(Reporter.content(request, request.getCharacterEncoding()));
            sb.append("\n\n\nResponse violations: ").append(report.getResponseViolations()).append("\n\n");
            sb.append(formatHeaders(response.getHeaderValues())).append("\n");
            sb.append(Reporter.content(response, response.getCharacterEncoding()));
            return sb.toString();
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
        public String formatUsage(Reporter reporter, String key, DescribedUsage describedUsage) throws IOException {
            Map<String, Object> json = new HashMap<>();
            json.put("context", key);
            json.put("unused", describedUsage.asMap());
            return OBJECT_MAPPER.writeValueAsString(json);
        }

        @Override
        public String formatViolations(Reporter reporter, long idValue, RamlReport report, ServletRamlRequest request, ServletRamlResponse response) throws IOException {
            Map<String, Object> json = new HashMap<>();
            json.put("id", idValue);
            json.put("request violations", Lists.newArrayList(report.getRequestViolations()));
            json.put("request", Reporter.formatRequest(request));
            json.put("request headers", request.getHeaderValues().asMap());
            json.put("response violations", Lists.newArrayList(report.getResponseViolations()));
            json.put("response", Reporter.content(response, response.getCharacterEncoding()));
            json.put("response headers", response.getHeaderValues().asMap());
            return OBJECT_MAPPER.writeValueAsString(json);
        }

    };

    final String fileExtension;

    private ReportFormat(String fileExtension) {
        this.fileExtension = fileExtension;
    }

    public abstract String formatUsage(Reporter reporter, String key, DescribedUsage describedUsage) throws IOException;

    public abstract String formatViolations(Reporter reporter, long idValue, RamlReport report, ServletRamlRequest request, ServletRamlResponse response) throws IOException;
}
