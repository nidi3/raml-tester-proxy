package guru.nidi.ramlproxy.undertow;

import guru.nidi.ramltester.model.RamlRequest;
import guru.nidi.ramltester.model.Values;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HeaderValues;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Deque;
import java.util.Map;

/**
 *
 */
public class UndertowRamlRequest implements RamlRequest {
    private final HttpServerExchange delegate;

    public UndertowRamlRequest(HttpServerExchange delegate) {
        this.delegate = delegate;
    }

    @Override
    public String getRequestUrl(String baseUri) {
        return null;
    }

    @Override
    public String getMethod() {
        return delegate.getRequestMethod().toString();
    }

    @Override
    public Values getQueryValues() {
        final Values values = new Values();
        for (Map.Entry<String, Deque<String>> entry : delegate.getQueryParameters().entrySet()) {
            values.addValues(entry.getKey(), entry.getValue());
        }
        return values;
    }

    @Override
    public Values getFormValues() {
        return null;
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
