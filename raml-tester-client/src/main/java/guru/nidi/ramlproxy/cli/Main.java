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
package guru.nidi.ramlproxy.cli;

import guru.nidi.ramlproxy.RamlProxy;
import guru.nidi.ramlproxy.Version;
import guru.nidi.ramlproxy.core.ClientOptions;
import guru.nidi.ramlproxy.core.ServerOptions;
import guru.nidi.ramltester.RamlDefinition;
import guru.nidi.ramltester.core.RamlReport;
import guru.nidi.ramltester.core.RamlViolationMessage;
import org.eclipse.jetty.util.ssl.SslContextFactory;

import java.io.IOException;
import java.net.ConnectException;

public class Main {
    public static void main(String... args) throws Exception {
        System.out.println("RAML tester proxy. Version " + Version.VERSION);
        if (args.length == 0) {
            showHelp();
            System.exit(1);
        }

        if (args[0].startsWith("-")) {
            startServer(args);
        } else {
            executeCommand(new ClientOptionsParser().fromCli(args));
        }
    }

    private static void startServer(String[] args) throws Exception {
        LogConfigurer.config();

        final ServerOptions options = new ServerOptionsParser().fromCli(args);
        if (options.isAsyncMode()) {
            RamlProxy.startServerAsync(options);
        } else {
            RamlProxy.prestartServer(options.getPort());
            final RamlDefinition definition = validate(options);
            if (options.isValidationOnly()) {
                return;
            }
            if (!options.isMockMode()) {
                initSslFactory();
            }
            RamlProxy.startServerSync(options, definition).waitForServer();
        }
    }

    private static RamlDefinition validate(ServerOptions options) {
        final RamlDefinition definition = options.fetchRamlDefinition();
        final RamlReport validate = options.validateRaml(definition);
        if (!validate.getValidationViolations().isEmpty()) {
            System.out.println("The RAML file has validation errors:");
            for (final RamlViolationMessage violation : validate.getValidationViolations()) {
                System.out.println("- " + violation);
            }
        }
        return definition;
    }

    private static void initSslFactory() {
        runAsync(new Runnable() {
            @Override
            public void run() {
                final SslContextFactory factory = new SslContextFactory();
                try {
                    factory.start();
                    factory.newSSLEngine();
                } catch (Exception e) {
                    //ignore
                }
            }
        });
    }

    private static void runAsync(Runnable runnable) {
        final Thread thread = new Thread(runnable);
        thread.setDaemon(true);
        thread.start();
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
            System.out.println(RamlProxy.executeRawCommand(options));
        } catch (ConnectException e) {
            System.out.println("Could not connect to proxy on port " + options.getPort() + ". Use correct port or start a new proxy.");
        } catch (IOException e) {
            System.out.println("Problem executing command: " + e.getMessage());
        }
    }

}
