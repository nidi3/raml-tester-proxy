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

import guru.nidi.ramlproxy.*;
import guru.nidi.ramlproxy.report.ReportSaver;
import guru.nidi.ramlproxy.report.Reporter;

import java.io.IOException;
import java.net.ConnectException;
import java.util.List;

/**
 *
 */
public class Main {
    public static void main(String[] args) throws Exception {
        final Main main = new Main();
        if (args.length == 0) {
            main.showHelp();
            System.exit(1);
        }

        LogConfigurer.init();

        if (args[0].startsWith("-")) {
            main.startServer(args);
        } else {
            main.executeCommand(args);
        }
    }

    private void showHelp() {
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

    private void startServer(String[] args) throws Exception {
        final ServerOptionsParser sop = new ServerOptionsParser();
        final ServerOptions options = sop.fromCli(args);
        if (options.isAsyncMode()) {
            final String classPath = findJarFile();
            stopRunningServer(options.getPort());
            startNewServer(classPath, sop.argsWithoutAsync(args));
        } else {
            stopRunningServer(options.getPort());
            final ReportSaver saver = new Reporter(options.getSaveDir(), options.getFileFormat());
            final RamlProxy proxy = RamlProxy.create(saver, options);
            proxy.waitForServer();
        }
    }

    private String findJarFile() {
        final String classPath = System.getProperty("java.class.path");
        if (!classPath.endsWith(".jar")) {
            System.out.println("Cannot run in asynchronous mode without jar");
            System.exit(1);
        }
        return classPath;
    }

    private void startNewServer(String jar, List<String> args) throws IOException, InterruptedException {
        final SubProcess subProcess = new SubProcess(jar, args);
        String line;
        do {
            line = subProcess.readLine();
            System.out.println(line);
        } while (!line.endsWith("started"));
    }

    private void stopRunningServer(int port) {
        final CommandSender sender = new CommandSender(port);
        try {
            sender.sendGetResponse(Command.STOP, null);
        } catch (IOException e) {
            //ignore
        }
    }

    private void executeCommand(String[] args) {
        final ClientOptionsParser cop = new ClientOptionsParser();
        final ClientOptions options = cop.fromCli(args);
        final CommandSender sender = new CommandSender(options.getPort());
        try {
            final String result = sender.sendGetResponse(options.getCommand(), queryString(options));
            System.out.println(result);
        } catch (ConnectException e) {
            System.out.println("Could not connect to proxy, start a new one.");
        } catch (IOException e) {
            System.out.println("Problem executing command: " + e.getMessage());
        }
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

}
