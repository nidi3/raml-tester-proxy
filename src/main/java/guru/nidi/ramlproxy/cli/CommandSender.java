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

import guru.nidi.ramlproxy.Command;
import guru.nidi.ramlproxy.TesterFilter;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

/**
 *
 */
public class CommandSender {
    protected final HttpClient client;
    protected final int port;

    public CommandSender(int port) {
        this.port = port;
        client = createClient();
    }

    protected HttpClient createClient() {
        final RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(1000)
                .setConnectionRequestTimeout(1000)
                .setSocketTimeout(1000)
                .build();
        return HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .build();
    }

    public String send(Command command, String query) throws IOException {
        final HttpResponse response = sendGet(command, query);
        return content(response);
    }

    public HttpResponse sendGet(Command command, String query) throws IOException {
        final String url = host() + commandPath(command) + (query == null ? "" : "?" + query);
        final HttpGet get = new HttpGet(url);
        return client.execute(get);
    }

    public String host() {
        return "http://localhost:" + port;
    }

    protected String commandPath(Command command) {
        return TesterFilter.COMMAND_PATH + "/" + command.getName();
    }

    public static String content(HttpResponse response) throws IOException {
        return EntityUtils.toString(response.getEntity());
    }

}
