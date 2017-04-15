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
package guru.nidi.ramlproxy;

import guru.nidi.ramlproxy.core.RamlProxyServer;
import guru.nidi.ramlproxy.core.ServerOptions;
import guru.nidi.ramlproxy.report.ReportSaver;
import guru.nidi.ramltester.core.RamlReport;
import guru.nidi.ramltester.servlet.ServletRamlRequest;
import guru.nidi.ramltester.servlet.ServletRamlResponse;

/**
 * Debug real-life applications, not real test.
 */
public class LiveDebug {
    public static void main(String... args) throws Exception {
        startTomcat();
//        Thread.sleep(1000000);
        startProxy();
    }

    private static void startTomcat() throws Exception {
        new TomcatServer(25001, new SimpleServlet());
    }

    private static void startProxy() throws Exception {
        final ReportSaver loggingSaver = new ReportSaver() {
            @Override
            protected void addingReport(RamlReport report, ServletRamlRequest request, ServletRamlResponse response) {
                System.out.println(report);
            }
        };

        try (final RamlProxyServer proxy = RamlProxy.startServerSync(
                new ServerOptions(8099, "http://localhost:25001", "file://raml-tester-client/src/test/resources/guru/nidi/ramlproxy/test.raml", "http://mobile.youbook.com/api"),
//                new ServerOptions(8099, "../youbook-mobile/test/mock-data", "file://../youbook-mobile/test/api/api.raml", "http://mobile.youbook.com/api"),
                loggingSaver)) {
            proxy.waitForServer();
        }

//        try (final RamlProxyServer proxy =RamlProxy.startServerSync(
//                new ServerOptions(8099, "../raml-tester-uc-js/test/data", "file://../raml-tester-uc-js/test/data.raml", "http://raml.nidi.guru"),
// loggingSaver)){
//            proxy.waitForServer();
//        }
    }
}
