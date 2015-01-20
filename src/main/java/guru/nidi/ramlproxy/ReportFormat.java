package guru.nidi.ramlproxy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import guru.nidi.ramltester.core.RamlReport;
import guru.nidi.ramltester.model.Values;
import guru.nidi.ramltester.servlet.ServletRamlRequest;
import guru.nidi.ramltester.servlet.ServletRamlResponse;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
*
*/
public enum ReportFormat {
    TEXT("log") {
        @Override
        String formatUsage(Reporter reporter, String key, Map<String, Set<String>> unuseds) throws IOException {
            return reporter.usageToString(unuseds);
        }

        @Override
        String formatViolations(Reporter reporter, long idValue, RamlReport report, ServletRamlRequest request, ServletRamlResponse response) throws IOException {
            return violationsToString(report, request, response);
        }

        private String violationsToString(RamlReport report, ServletRamlRequest request, ServletRamlResponse response) throws IOException {
            StringBuilder sb = new StringBuilder();
            sb.append("Request violations: ").append(report.getRequestViolations()).append("\n\n");
            sb.append(Reporter.formatRequest(request)).append("\n");
            sb.append(formatHeaders(request.getHeaderValues())).append("\n");
            sb.append(request.getContent() == null ? Reporter.NO_CONTENT : new String(request.getContent(), encoding(request)));
            sb.append("\n\n\nResponse violations: ").append(report.getResponseViolations()).append("\n\n");
            sb.append(formatHeaders(response.getHeaderValues())).append("\n");
            sb.append(response.getContent() == null ? Reporter.NO_CONTENT : new String(response.getContent(), encoding(response)));
            return sb.toString();
        }

        private String encoding(ServletRamlRequest request) {
            return StringUtils.defaultIfBlank(request.getCharacterEncoding(), Charset.defaultCharset().name());
        }

        private String encoding(ServletRamlResponse response) {
            return StringUtils.defaultIfBlank(response.getCharacterEncoding(), Charset.defaultCharset().name());
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
        String formatUsage(Reporter reporter, String key, Map<String, Set<String>> unuseds) throws IOException {
            return usageToJson(key, unuseds);
        }

        protected String usageToJson(String key, Map<String, Set<String>> unuseds) throws IOException {
            Map<String, Object> json = new HashMap<>();
            json.put("context", key);
            json.put("unused", unuseds);
            return OBJECT_MAPPER.writeValueAsString(json);
        }

        @Override
        String formatViolations(Reporter reporter, long idValue, RamlReport report, ServletRamlRequest request, ServletRamlResponse response) throws IOException {
            return violationsToJson(idValue, report, request, response);
        }

        private String violationsToJson(long idValue, RamlReport report, ServletRamlRequest request, ServletRamlResponse response) throws IOException {
            Map<String, Object> json = new HashMap<>();
            json.put("id", idValue);
            json.put("request violations", Lists.newArrayList(report.getRequestViolations()));
            json.put("request", Reporter.formatRequest(request));
            json.put("request headers", request.getHeaderValues());
            json.put("response violations", Lists.newArrayList(report.getResponseViolations()));
            json.put("response", (response.getContent() == null ? Reporter.NO_CONTENT : response.getContent()));
            json.put("response headers", response.getHeaderValues());
            return OBJECT_MAPPER.writeValueAsString(json);
        }

    };

    final String fileExtension;

    private ReportFormat(String fileExtension) {
        this.fileExtension = fileExtension;
    }

    abstract String formatUsage(Reporter reporter, String key, Map<String, Set<String>> unuseds) throws IOException;

    abstract String formatViolations(Reporter reporter, long idValue, RamlReport report, ServletRamlRequest request, ServletRamlResponse response) throws IOException;
}
