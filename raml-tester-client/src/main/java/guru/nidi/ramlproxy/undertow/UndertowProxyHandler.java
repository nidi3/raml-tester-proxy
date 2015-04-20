package guru.nidi.ramlproxy.undertow;

import guru.nidi.ramlproxy.core.TesterFilter;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.proxy.ProxyHandler;
import io.undertow.server.handlers.proxy.SimpleProxyClientProvider;

import java.net.URI;

/**
 *
 */
public class UndertowProxyHandler implements HttpHandler {
    private final TesterFilter testerFilter;
    private final ProxyHandler proxy;

    public UndertowProxyHandler(String target, TesterFilter testerFilter) {
        proxy = new ProxyHandler(new SimpleProxyClientProvider(URI.create(target)), new HttpHandler() {
            @Override
            public void handleRequest(HttpServerExchange exchange) throws Exception {
                exchange.getResponseSender().send("udsjds");
            }
        });
        this.testerFilter = testerFilter;
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
            testerFilter.delay();
        exchange.getRequestHeaders().remove("Host");
        proxy.handleRequest(exchange);
//        testerFilter.test(new UndertowRamlRequest(exchange),new UndertowRamlResponse(exchange));
    }
}
