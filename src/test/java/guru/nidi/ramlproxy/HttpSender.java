package guru.nidi.ramlproxy;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;
import java.io.IOException;

/**
 *
 */
public class HttpSender {
    private final HttpClient client;
    private final int port;

    public HttpSender(int port) {
        this.port = port;
        client = HttpClientBuilder.create().setSSLHostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String s, SSLSession sslSession) {
                return true;
            }
        }).build();
    }

    public String url(String path) {
        return "http://localhost:" + port + "/" + path;
    }

    public int getPort() {
        return port;
    }

    public String executeGet(String path) throws IOException {
        final HttpGet get = new HttpGet(url(path));
        final HttpResponse response = client.execute(get);
        return EntityUtils.toString(response.getEntity());
    }

    public String executeGet(RamlProxy<?> proxy, String path) throws Exception {
        proxy.start();
        final String res = executeGet(path);
        proxy.stop();
        return res;
    }

}
