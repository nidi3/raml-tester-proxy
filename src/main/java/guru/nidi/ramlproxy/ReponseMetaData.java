package guru.nidi.ramlproxy;

import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class ReponseMetaData {
    private final int code;
    private final Map<String, List<Object>> headers;

    public ReponseMetaData(int code, Map<String, List<Object>> headers) {
        this.code = code;
        this.headers = headers;
    }

    public ReponseMetaData(Map<String, Object> raw) {
        code = raw.get("code") == null ? HttpServletResponse.SC_OK : (Integer) raw.get("code");
        headers = new HashMap<>();
        final Map<String, Object> rawHeaders = (Map<String, Object>) raw.get("headers");
        if (rawHeaders != null) {
            for (Map.Entry<String, Object> entry : rawHeaders.entrySet()) {
                if (entry.getValue() instanceof List) {
                    headers.put(entry.getKey(), (List<Object>) entry.getValue());
                } else {
                    headers.put(entry.getKey(), Collections.singletonList(entry.getValue()));
                }
            }
        }
    }

    public void apply(HttpServletResponse response) {
        response.setStatus(code);
        for (Map.Entry<String, List<Object>> entry : headers.entrySet()) {
            for (Object value : entry.getValue()) {
                response.addHeader(entry.getKey(), "" + value);
            }
        }
    }
}
