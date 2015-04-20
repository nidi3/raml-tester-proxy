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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class CollectionUtils {
//    public static void main(final String[] args) throws Exception {
//        long a = System.currentTimeMillis();
//
//        Undertow server = Undertow.builder()
//                .addHttpListener(8080, "localhost")
//                .setHandler(new ProxyHandler(new SimpleProxyClientProvider(URI.create("uri")), new HttpHandler() {
//                    @Override
//                    public void handleRequest(HttpServerExchange exchange) throws Exception {
//                        exchange.setResponseCode(404);
//                    }
//
//                })).build();
//                        .setHandler(new HttpHandler() {
//                            @Override
//                            public void handleRequest(final HttpServerExchange exchange) throws Exception {
//                                exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
//                                exchange.getResponseSender().send("Hello World");
//                            }
//                        }).build();
//        server.start();
//        System.out.println(System.currentTimeMillis() - a);
//        server.stop();
//
//        try (RamlProxyServer proxy = RamlProxy.startServerSync(new ServerOptions(
//                8099, Ramls.MOCK_DIR, Ramls.SIMPLE, "http://nidi.guru/raml", new File("target"), null, true), new ReportSaver())) {
//
//        }
//    }

    public static Map map(Object... keysValues) {
        final Map<Object, Object> map = new HashMap<>();
        for (int i = 0; i < keysValues.length; i += 2) {
            map.put(keysValues[i], keysValues[i + 1]);
        }
        return map;
    }

    public static List list(Object... values) {
        return Arrays.asList(values);
    }
}
