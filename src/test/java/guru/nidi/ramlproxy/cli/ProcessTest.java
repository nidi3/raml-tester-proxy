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
package guru.nidi.ramlproxy.cli;

import guru.nidi.ramlproxy.HttpSender;
import guru.nidi.ramlproxy.Ramls;
import guru.nidi.ramlproxy.SimpleServlet;
import guru.nidi.ramlproxy.TomcatServer;
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
        try (final TestSubProcess proxy = new TestSubProcess("-p", "" + sender.getPort(), "-t", "https://api.github.com", "-r", Ramls.GITHUB)) {
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
        try (final TestSubProcess proxy = new TestSubProcess("-p" + sender.getPort(), "-thttps://api.github.com", "-r" + Ramls.GITHUB, "-i")) {
            assertProxyStarted(proxy);

            System.out.println(sender.contentOfGet("meta"));

            assertNull(proxy.readLine());
        }
    }

    @Test
    public void testRamlFromHttp() throws Exception {
        try (final TomcatServer tomcat = new TomcatServer(8083, new SimpleServlet());
             final TestSubProcess proxy = new TestSubProcess(
                     "-p", "" + sender.getPort(), "-t", "https://api.github.com", "-i",
                     "-r", tomcat.url() + "/resources/github-meta.raml")) {
            assertProxyStarted(proxy);

            System.out.println(sender.contentOfGet("meta"));

            assertNull(proxy.readLine());
        }
    }

    @Test
    public void testStopCommand() throws Exception {
        try (final TestSubProcess proxy = new TestSubProcess("-p", "" + sender.getPort(), "-t", "https://api.github.com", "-i", "-r", Ramls.GITHUB)) {
            assertProxyStarted(proxy);
            assertFalse(proxy.hasEnded());

            assertThat(sender.contentOfGet("@@@proxy/stop"), equalTo("Stopping proxy"));
            assertThat(proxy.readLine(), endsWith("Stopping proxy"));
            for (int i = 0; i < 10 && !proxy.hasEnded(); i++) {
                Thread.sleep(50);
            }
            assertTrue(proxy.hasEnded());
        }
    }

    private void assertProxyStarted(TestSubProcess proxy) throws InterruptedException {
        assertThat(proxy.readLine(15), containsString("eporting"));
        assertThat(proxy.readLine(15), endsWith("Proxy started"));
    }
}
