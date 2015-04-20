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

import org.apache.http.client.methods.HttpRequestBase;
import org.eclipse.jetty.client.api.Request;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 */
public enum CommandDecorators {
    IGNORE_COMMANDS("X-Ignore-Commands", true) {
        @Override
        public String set(Object req, HttpServletResponse res) {
            ((HttpRequestBase) req).addHeader(getName(), "true");
            return super.set(req, res);
        }
    },
    CLEAR_REPORTS("clear-reports", false),
    CLEAR_USAGE("clear-usage", false),
    ALLOW_ORIGIN("Access-Control-Allow-Origin", true) {
        @Override
        public String set(Object req, HttpServletResponse res) {
            final String origin = ((HttpServletRequest) req).getHeader("Origin");
            if (origin != null) {
                res.setHeader(getName(), origin);
            }
            return origin;
        }
    };

    private final String name;
    private final boolean header;

    CommandDecorators(String name, boolean header) {
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

    public String set(Object req, HttpServletResponse res) {
        return getName() + "=true";
    }

    public void removeFrom(Request proxyRequest) {
        proxyRequest.getHeaders().remove(name);
    }

    public String getName() {
        return name;
    }
}
