package guru.nidi.ramlproxy;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;
import java.io.IOException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.*;

/**
 *
 */
public class SimpleProcessTest extends AbstractProcessTest {
    private HttpClient client;

    @Before
    public void init() {
        client = HttpClientBuilder.create().setSSLHostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String s, SSLSession sslSession) {
                return true;
            }
        }).build();
    }

    @After
    public void stop() {
        stopProxyProcess();
    }

    @Test
    public void testGithub() throws IOException, InterruptedException {
        startProxyProcess("-p", "8080", "-t", "https://api.github.com",
                "-r", "file://src/test/resources/guru/nidi/ramlproxy/github-meta.raml");
        final HttpGet get = new HttpGet("http://localhost:8080/meta");
        final HttpResponse response = client.execute(get);
        System.out.println(EntityUtils.toString(response.getEntity()));

        assertNotNull(readLine());
        assertThat(readLine(), equalTo("           Request:  []"));
        assertThat(readLine(), startsWith("           Response: [Header 'X-"));
        assertNull(readLine());
    }

    @Test
    public void testGithubIgnoringX() throws IOException, InterruptedException {
        startProxyProcess("-p", "8080", "-t", "https://api.github.com", "-i",
                "-r", "file://src/test/resources/guru/nidi/ramlproxy/github-meta.raml");

        final HttpGet get = new HttpGet("http://localhost:8080/meta");
        final HttpResponse response = client.execute(get);
        System.out.println(EntityUtils.toString(response.getEntity()));

        assertNull(readLine());
    }
}
