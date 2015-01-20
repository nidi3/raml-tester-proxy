package guru.nidi.ramlproxy;

import guru.nidi.ramltester.core.Usage;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 */
class DescribedUsage {
    private final Map<String, Set<String>> unuseds = new HashMap<>();

    public DescribedUsage(Usage usage) {
        add(usage.getUnusedResources(), "resources");
        add(usage.getUnusedActions(), "actions");
        add(usage.getUnusedRequestHeaders(), "request headers");
        add(usage.getUnusedQueryParameters(), "query parameters");
        add(usage.getUnusedFormParameters(), "form parameters");
        add(usage.getUnusedResponseHeaders(), "response headers");
        add(usage.getUnusedResponseCodes(), "response codes");
    }

    private void add(Set<String> values, String desc) {
        if (!values.isEmpty()) {
            unuseds.put(desc, values);
        }
    }

    public Map<String, Set<String>> asMap() {
        return unuseds;
    }

    public String toString() {
        final StringBuilder sb = new StringBuilder();
        for (final Map.Entry<String, Set<String>> unused : unuseds.entrySet()) {
            sb.append("  Unused ").append(unused.getKey()).append("\n");
            for (final String value : unused.getValue()) {
                sb.append("    ").append(value).append("\n");
            }
        }
        return sb.toString();
    }
}
