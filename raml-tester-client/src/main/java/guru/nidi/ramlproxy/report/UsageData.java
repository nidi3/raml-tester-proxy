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

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 *
 */
public class UsageData {
    private final List<String> unusedActions;
    private final List<String> unusedResources;
    private final List<String> unusedRequestHeaders;
    private final List<String> unusedQueryParameters;
    private final List<String> unusedFormParameters;
    private final List<String> unusedResponseHeaders;
    private final List<String> unusedResponseCodes;

    public UsageData(@JsonProperty("unusedActions") Collection<String> unusedActions,
                     @JsonProperty("unusedResources") Collection<String> unusedResources,
                     @JsonProperty("unusedRequestHeaders") Collection<String> unusedRequestHeaders,
                     @JsonProperty("unusedQueryParameters") Collection<String> unusedQueryParameters,
                     @JsonProperty("unusedFormParameters") Collection<String> unusedFormParameters,
                     @JsonProperty("unusedResponseHeaders") Collection<String> unusedResponseHeaders,
                     @JsonProperty("unusedResponseCodes") Collection<String> unusedResponseCodes) {
        this.unusedActions = new ArrayList<>(unusedActions);
        this.unusedResources = new ArrayList<>(unusedResources);
        this.unusedRequestHeaders = new ArrayList<>(unusedRequestHeaders);
        this.unusedQueryParameters = new ArrayList<>(unusedQueryParameters);
        this.unusedFormParameters = new ArrayList<>(unusedFormParameters);
        this.unusedResponseHeaders = new ArrayList<>(unusedResponseHeaders);
        this.unusedResponseCodes = new ArrayList<>(unusedResponseCodes);
    }

    public List<String> getUnusedActions() {
        return unusedActions;
    }

    public List<String> getUnusedResources() {
        return unusedResources;
    }

    public List<String> getUnusedRequestHeaders() {
        return unusedRequestHeaders;
    }

    public List<String> getUnusedQueryParameters() {
        return unusedQueryParameters;
    }

    public List<String> getUnusedFormParameters() {
        return unusedFormParameters;
    }

    public List<String> getUnusedResponseHeaders() {
        return unusedResponseHeaders;
    }

    public List<String> getUnusedResponseCodes() {
        return unusedResponseCodes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        UsageData usageData = (UsageData) o;

        if (!unusedActions.equals(usageData.unusedActions)) {
            return false;
        }
        if (!unusedResources.equals(usageData.unusedResources)) {
            return false;
        }
        if (!unusedRequestHeaders.equals(usageData.unusedRequestHeaders)) {
            return false;
        }
        if (!unusedQueryParameters.equals(usageData.unusedQueryParameters)) {
            return false;
        }
        if (!unusedFormParameters.equals(usageData.unusedFormParameters)) {
            return false;
        }
        if (!unusedResponseHeaders.equals(usageData.unusedResponseHeaders)) {
            return false;
        }
        return unusedResponseCodes.equals(usageData.unusedResponseCodes);

    }

    @Override
    public int hashCode() {
        int result = unusedActions.hashCode();
        result = 31 * result + unusedResources.hashCode();
        result = 31 * result + unusedRequestHeaders.hashCode();
        result = 31 * result + unusedQueryParameters.hashCode();
        result = 31 * result + unusedFormParameters.hashCode();
        result = 31 * result + unusedResponseHeaders.hashCode();
        result = 31 * result + unusedResponseCodes.hashCode();
        return result;
    }
}
