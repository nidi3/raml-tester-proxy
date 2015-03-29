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

import guru.nidi.ramlproxy.ClientOptions;
import guru.nidi.ramlproxy.RamlProxy;
import guru.nidi.ramlproxy.ServerOptions;

import java.io.IOException;
import java.net.ConnectException;

/**
 *
 */
public class Main {
    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            showHelp();
            System.exit(1);
        }

        LogConfigurer.init();

        if (args[0].startsWith("-")) {
            final ServerOptions options = new ServerOptionsParser().fromCli(args);
            if (options.isAsyncMode()) {
                RamlProxy.startServerAsync(options);
            } else {
                RamlProxy.startServerSync(options).waitForServer();
            }
        } else {
            executeCommand(new ClientOptionsParser().fromCli(args));
        }
    }

    private static void showHelp() {
        System.out.println("Missing arguments.");
        System.out.println();
        System.out.println("Start a proxy:");
        System.out.println("--------------");
        new ServerOptionsParser().showHelp();
        System.out.println();
        System.out.println("Execute a command on a running proxy:");
        System.out.println("-------------------------------------");
        new ClientOptionsParser().showHelp();
    }

    private static void executeCommand(ClientOptions options) {
        try {
            System.out.println(RamlProxy.executeCommand(options));
        } catch (ConnectException e) {
            System.out.println("Could not connect to proxy, start a new one.");
        } catch (IOException e) {
            System.out.println("Problem executing command: " + e.getMessage());
        }
    }

}
