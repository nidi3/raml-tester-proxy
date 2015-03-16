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

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
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

    public String content(HttpResponse response) throws IOException {
        return EntityUtils.toString(response.getEntity());
    }

    public String contentOfGet(String path) throws IOException {
        final HttpGet get = new HttpGet(url(path));
        final HttpResponse response = client.execute(get);
        return content(response);
    }

    public String contentOfGet(RamlProxy<?> proxy, String path) throws Exception {
        proxy.start();
        final String res = contentOfGet(path);
        proxy.stop();
        return res;
    }

    public HttpResponse get(String path) throws IOException {
        final HttpGet get = new HttpGet(url(path));
        return client.execute(get);
    }

    public HttpResponse post(String path) throws IOException {
        final HttpPost post = new HttpPost(url(path));
        return client.execute(post);
    }

}
