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
package guru.nidi.ramlproxy.core;

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
            for (final Map.Entry<String, Object> entry : rawHeaders.entrySet()) {
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
        for (final Map.Entry<String, List<Object>> entry : headers.entrySet()) {
            for (final Object value : entry.getValue()) {
                response.addHeader(entry.getKey(), "" + value);
            }
        }
    }
}
