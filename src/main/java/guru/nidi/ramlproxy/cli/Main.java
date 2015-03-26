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
import guru.nidi.ramlproxy.report.ReportSaver;
import guru.nidi.ramlproxy.report.Reporter;

/**
 *
 */
public class Main {
    public static void main(String[] args) throws Exception {
        final ServerOptionsParser sop = new ServerOptionsParser();
        final ClientOptionsParser cop = new ClientOptionsParser();
        if (args.length == 0) {
            System.out.println("Missing arguments.");
            System.out.println();
            System.out.println("Start a proxy:");
            System.out.println("--------------");
            sop.showHelp();
            System.out.println();
            System.out.println("Execute a command on a running proxy:");
            System.out.println("-------------------------------------");
            cop.showHelp();
            System.exit(1);
        } else {
            LogConfigurer.init();

            if (args[0].startsWith("-")) {
                final ServerOptions options = sop.fromCli(args);
                final ReportSaver saver = new Reporter(options.getSaveDir(), options.getFileFormat());
                final RamlProxy proxy = RamlProxy.create(saver, options);
                proxy.waitForServer();
            } else {
                final ClientOptions options = cop.fromCli(args);
            }
        }

    }
}
