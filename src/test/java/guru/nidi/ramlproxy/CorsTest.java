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

import guru.nidi.ramlproxy.report.ReportSaver;
import org.apache.http.HttpResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;

import static guru.nidi.ramlproxy.cli.CommandSender.content;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 *
 */
public class CorsTest {

    private static final String ORIGIN = "http://cors/origin";
    private HttpSender mockSender = new HttpSender(8091);
    private HttpSender proxySender = new HttpSender(8090);
    private static RamlProxy mock, proxy;

    @Before
    public void init() throws Exception {
        mock = RamlProxy.create(new ReportSaver(), new ServerOptions(
                mockSender.getPort(), Ramls.MOCK_DIR, Ramls.SIMPLE, "http://nidi.guru/raml", new File("target"), null, true));
        proxy = RamlProxy.create(new ReportSaver(), new ServerOptions(
                proxySender.getPort(), mockSender.host(), Ramls.COMMAND, null));
    }

    @After
    public void end() throws Exception {
        mock.close();
        proxy.close();
    }

    @Test
    @Ignore
    public void cors() throws Exception {
        try (final RamlProxy proxy = RamlProxy.create(new ReportSaver(),
                new ServerOptions(8090, "../raml-tester-uc-js/test/data", "file://../raml-tester-uc-js/test/data.raml", "http://raml.nidi.guru"))) {
            proxy.waitForServer();
        }
    }

    @Test
    public void corsInMock() throws Exception {
        final HttpResponse response = mockSender.corsGet("v1/data", ORIGIN);
        assertEquals(ORIGIN, response.getFirstHeader("Access-Control-Allow-Origin").getValue());
    }

    @Test
    public void noCorsInMock() throws Exception {
        final HttpResponse response = mockSender.get("v1/data");
        assertNull(response.getFirstHeader("Access-Control-Allow-Origin"));
    }

    @Test
    public void corsInCommand() throws Exception {
        final HttpResponse response = proxySender.corsGet("@@@proxy/ping", ORIGIN);
        assertEquals("Pong", content(response));
        assertEquals(ORIGIN, response.getFirstHeader("Access-Control-Allow-Origin").getValue());
    }

    @Test
    public void noCorsInCommand() throws Exception {
        final HttpResponse response = proxySender.get("@@@proxy/ping");
        assertEquals("Pong", content(response));
        assertNull(response.getFirstHeader("Access-Control-Allow-Origin"));
    }

    @Test
    public void corsInProxy() throws Exception {
        final HttpResponse response = proxySender.corsGet("v1/data", ORIGIN);
        assertEquals(ORIGIN, response.getFirstHeader("Access-Control-Allow-Origin").getValue());
    }

    @Test
    public void noCorsInProxy() throws Exception {
        final HttpResponse response = proxySender.get("v1/data");
        assertNull(response.getFirstHeader("Access-Control-Allow-Origin"));
    }

}
