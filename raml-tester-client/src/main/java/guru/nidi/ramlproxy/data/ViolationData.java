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
package guru.nidi.ramlproxy.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import guru.nidi.ramltester.core.RamlViolations;

import java.util.List;
import java.util.Map;

import static guru.nidi.ramlproxy.data.Converters.violationMessages;

public class ViolationData {
    private final long id;
    private final String request;
    private final Map<String, List<Object>> requestHeaders;
    private final List<String> requestViolations;
    private final String response;
    private final Map<String, List<Object>> responseHeaders;
    private final List<String> responseViolations;

    public ViolationData(@JsonProperty("id") long id,
                         @JsonProperty("request") String request,
                         @JsonProperty("requestHeaders") Map<String, List<Object>> requestHeaders,
                         @JsonProperty("requestViolations") List<String> requestViolations,
                         @JsonProperty("response") String response,
                         @JsonProperty("responseHeaders") Map<String, List<Object>> responseHeaders,
                         @JsonProperty("responseViolations") List<String> responseViolations) {
        this.id = id;
        this.request = request;
        this.requestHeaders = requestHeaders;
        this.requestViolations = requestViolations;
        this.response = response;
        this.responseHeaders = responseHeaders;
        this.responseViolations = responseViolations;
    }

    public static ViolationData of(long id,
                                   String request, Map<String, List<Object>> requestHeaders,
                                   RamlViolations requestViolations,
                                   String response, Map<String, List<Object>> responseHeaders,
                                   RamlViolations responseViolations) {
        return new ViolationData(id,
                request, requestHeaders, violationMessages(requestViolations),
                response, responseHeaders, violationMessages(responseViolations));
    }

    public long getId() {
        return id;
    }

    public String getRequest() {
        return request;
    }

    public Map<String, List<Object>> getRequestHeaders() {
        return requestHeaders;
    }

    public List<String> getRequestViolations() {
        return requestViolations;
    }

    public String getResponse() {
        return response;
    }

    public Map<String, List<Object>> getResponseHeaders() {
        return responseHeaders;
    }

    public List<String> getResponseViolations() {
        return responseViolations;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final ViolationData data = (ViolationData) o;

        if (id != data.id) {
            return false;
        }
        if (!request.equals(data.request)) {
            return false;
        }
        if (!requestHeaders.equals(data.requestHeaders)) {
            return false;
        }
        if (!requestViolations.equals(data.requestViolations)) {
            return false;
        }
        if (!response.equals(data.response)) {
            return false;
        }
        if (!responseHeaders.equals(data.responseHeaders)) {
            return false;
        }
        return responseViolations.equals(data.responseViolations);

    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + request.hashCode();
        result = 31 * result + requestHeaders.hashCode();
        result = 31 * result + requestViolations.hashCode();
        result = 31 * result + response.hashCode();
        result = 31 * result + responseHeaders.hashCode();
        result = 31 * result + responseViolations.hashCode();
        return result;
    }
}
