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
package guru.nidi.ramlproxy.core;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

public class CommandSender {
    protected final HttpClient client;
    protected final int port;

    public CommandSender(int port) {
        this.port = port;
        client = createClient();
    }

    public CommandSender(ClientOptions options) {
        this(options.getPort());
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

    public <T> T send(Command command) throws IOException {
        return (T) command.decode(sendRaw(command));
    }

    public <T> T send(ClientOptions options) throws IOException {
        return (T) options.getCommand().decode(sendRaw(options));
    }

    public String sendRaw(Command command) throws IOException {
        return content(executeGet(createGet(command, null)));
    }

    public String sendRaw(ClientOptions options) throws IOException {
        return content(executeGet(createGet(options.getCommand(), queryString(options))));
    }

    protected HttpResponse executeGet(HttpGet get) throws IOException {
        return client.execute(get);
    }

    protected HttpGet createGet(Command command, String query) {
        return new HttpGet(host() + commandPath(command) + (query == null ? "" : "?" + query));
    }

    private String queryString(ClientOptions options) {
        String query = "";
        if (options.isClearReports()) {
            query += CommandDecorators.CLEAR_REPORTS.set(null, null);
        }
        if (options.isClearUsage()) {
            query += "&" + CommandDecorators.CLEAR_USAGE.set(null, null);
        }
        return query;
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
