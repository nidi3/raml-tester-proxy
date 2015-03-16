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

import guru.nidi.ramlproxy.TestUtils.Ramls;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

/**
 *
 */
public class ProcessTest {
    private HttpSender sender = new HttpSender(8082);

    @Test
    public void testGithub() throws IOException, InterruptedException {
        try (final ProxyProcess proxy = new ProxyProcess("-p", "" + sender.getPort(), "-t", "https://api.github.com", "-r", Ramls.GITHUB)) {
            assertProxyStarted(proxy);

            System.out.println(sender.contentOfGet("meta"));

            assertNotNull(proxy.readLine());
            assertThat(proxy.readLine(), equalTo("           Request:  []"));
            assertThat(proxy.readLine(), startsWith("           Response: [Header 'X-"));
            assertNull(proxy.readLine());
        }
    }

    @Test
    public void testGithubIgnoringX() throws IOException, InterruptedException {
        try (final ProxyProcess proxy = new ProxyProcess("-p" + sender.getPort(), "-thttps://api.github.com", "-r" + Ramls.GITHUB, "-i")) {
            assertProxyStarted(proxy);

            System.out.println(sender.contentOfGet("meta"));

            assertNull(proxy.readLine());
        }
    }

    @Test
    public void testRamlFromHttp() throws Exception {
        try (final TomcatServer tomcat = new TomcatServer(8083, new SimpleServlet());
             final ProxyProcess proxy = new ProxyProcess(
                     "-p", "" + sender.getPort(), "-t", "https://api.github.com", "-i",
                     "-r", tomcat.url() + "/resources/github-meta.raml")) {
            assertProxyStarted(proxy);

            System.out.println(sender.contentOfGet("meta"));

            assertNull(proxy.readLine());
        }
    }

    @Test
    public void testStopCommand() throws Exception {
        try (final ProxyProcess proxy = new ProxyProcess("-p", "" + sender.getPort(), "-t", "https://api.github.com", "-i", "-r", Ramls.GITHUB)) {
            assertProxyStarted(proxy);
            assertFalse(proxy.hasEnded());

            assertEquals("Stopping proxy", sender.contentOfGet("@@@proxy/stop"));
            assertEquals("Stopping proxy", proxy.readLine());
            Thread.sleep(100);
            assertTrue(proxy.hasEnded());
        }
    }

    private void assertProxyStarted(ProxyProcess proxy) throws InterruptedException {
        assertThat(proxy.readLine(15), containsString("eporting"));
        assertThat(proxy.readLine(15), endsWith("Proxy started"));
    }
}
