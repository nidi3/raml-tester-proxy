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
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;
import java.io.IOException;

/**
 *
 */
public class HttpSender extends CommandSender {
    private boolean ignoreCommands = false;

    public HttpSender(int port) {
        super(port);
    }

    @Override
    protected HttpClient createClient() {
        return HttpClientBuilder.create().setSSLHostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String s, SSLSession sslSession) {
                return true;
            }
        }).build();
    }

    public void setIgnoreCommands(boolean ignore) {
        this.ignoreCommands = ignore;
    }

    public String url(String path) {
        return path.startsWith("http") ? path : (host() + "/" + path);
    }

    public int getPort() {
        return port;
    }

    public String contentOfGet(String path) throws IOException {
        final HttpResponse response = get(path);
        return content(response);
    }

    public HttpResponse get(Command command) throws IOException {
        return get(commandPath(command));
    }

    public HttpResponse get(Command command, String query) throws IOException {
        return get(commandPath(command) + "?" + query);
    }

    public HttpResponse get(String path) throws IOException {
        return executeGet(new HttpGet(url(path)));
    }

    protected HttpResponse executeGet(HttpGet get) throws IOException {
        if (ignoreCommands) {
            CommandDecorators.IGNORE_COMMANDS.set(get, null);
        }
        return client.execute(get);
    }

    public HttpResponse corsGet(String path, String origin) throws IOException {
        final HttpGet get = new HttpGet(url(path));
        if (ignoreCommands) {
            CommandDecorators.IGNORE_COMMANDS.set(get, null);
        }
        get.setHeader("Origin", origin);
        return client.execute(get);
    }

    public HttpResponse post(String path, String data) throws IOException {
        final HttpPost post = new HttpPost(url(path));
        if (data != null) {
            post.setEntity(new StringEntity(data));
        }
        if (ignoreCommands) {
            CommandDecorators.IGNORE_COMMANDS.set(post, null);
        }
        return client.execute(post);
    }

}
