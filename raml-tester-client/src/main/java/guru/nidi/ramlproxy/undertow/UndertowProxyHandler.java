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
