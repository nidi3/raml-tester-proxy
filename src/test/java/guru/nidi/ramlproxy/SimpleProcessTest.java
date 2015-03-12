package guru.nidi.ramlproxy;

import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

/**
 *
 */
public class SimpleProcessTest {
    private HttpSender sender = new HttpSender(8082);

    @Test
    public void testGithub() throws IOException, InterruptedException {
        try (final ProxyProcess proxy = new ProxyProcess(
                "-p", "" + sender.getPort(), "-t", "https://api.github.com",
                "-r", "file://src/test/resources/guru/nidi/ramlproxy/github-meta.raml")) {
            assertProxyStarted(proxy);

            System.out.println(sender.executeGet("meta"));

            assertNotNull(proxy.readLine());
            assertThat(proxy.readLine(), equalTo("           Request:  []"));
            assertThat(proxy.readLine(), startsWith("           Response: [Header 'X-"));
            assertNull(proxy.readLine());
        }
    }

    @Test
    public void testGithubIgnoringX() throws IOException, InterruptedException {
        try (final ProxyProcess proxy = new ProxyProcess(
                "-p", "" + sender.getPort(), "-t", "https://api.github.com", "-i",
                "-r", "file://src/test/resources/guru/nidi/ramlproxy/github-meta.raml")) {
            assertProxyStarted(proxy);

            System.out.println(sender.executeGet("meta"));

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

            System.out.println(sender.executeGet("meta"));

            assertNull(proxy.readLine());
        }
    }

    private void assertProxyStarted(ProxyProcess proxy) throws InterruptedException {
        assertThat(proxy.readLine(15), containsString("eporting"));
        assertThat(proxy.readLine(15), endsWith("Proxy started"));
    }
}
