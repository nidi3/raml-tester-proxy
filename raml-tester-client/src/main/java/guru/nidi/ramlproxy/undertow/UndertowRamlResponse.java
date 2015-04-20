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
package guru.nidi.ramlproxy.undertow;

import guru.nidi.ramltester.model.RamlResponse;
import guru.nidi.ramltester.model.Values;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HeaderValues;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 */
public class UndertowRamlResponse implements RamlResponse {
    private final HttpServerExchange delegate;

    public UndertowRamlResponse(HttpServerExchange delegate) {
        this.delegate = delegate;
    }

    @Override
    public int getStatus() {
        return delegate.getResponseCode();
    }

    @Override
    public Values getHeaderValues() {
        final Values values = new Values();
        for (HeaderValues header : delegate.getRequestHeaders()) {
            values.addValues(header.getHeaderName().toString(), header);
        }
        return values;
    }

    @Override
    public String getContentType() {
        return delegate.getRequestHeaders().get("Content-Type").getFirst();
    }

    @Override
    public byte[] getContent() {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final InputStream in = delegate.startBlocking().getInputStream();
        final byte[] buf = new byte[1000];
        int read;
        try {
            while ((read = in.read(buf)) > 0) {
                out.write(buf, 0, read);
            }
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
