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

import org.eclipse.jetty.client.api.Request;

import javax.servlet.http.HttpServletRequest;

/**
 *
 */
enum CommandDecorators {
    IGNORE_COMMANDS("X-Ignore-Commands", true),
    CLEAR_REPORTS("clear-reports", false),
    CLEAR_USAGE("clear-usage", false);

    private final String name;
    private final boolean header;

    private CommandDecorators(String name, boolean header) {
        this.name = name;
        this.header = header;
    }

    public boolean isSet(HttpServletRequest request) {
        final String value;
        if (header) {
            value = request.getHeader(name);
        } else {
            value = request.getParameter(name);
        }
        return value != null && !value.equalsIgnoreCase("false");
    }

    public void removeFrom(Request proxyRequest) {
        proxyRequest.getHeaders().remove(name);
    }

    public String getName() {
        return name;
    }
}
