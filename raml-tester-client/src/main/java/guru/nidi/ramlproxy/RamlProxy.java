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

import guru.nidi.ramlproxy.report.ReportSaver;
import guru.nidi.ramlproxy.report.Reporter;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.IOException;

/**
 *
 */
public class RamlProxy {
    private static final RamlProxy INSTANCE = new RamlProxy();

    public static RamlProxyServer startServerSync(ServerOptions options) throws Exception {
        return startServerSync(options, new Reporter(options.getSaveDir(), options.getFileFormat()));
    }

    public static RamlProxyServer startServerSync(ServerOptions options, ReportSaver saver) throws Exception {
        INSTANCE.stopRunningServer(options.getPort());
        return new RamlProxyServer(saver, options);
    }

    public static SubProcess startServerAsync(ServerOptions options) throws Exception {
        final String classPath = INSTANCE.findJarFile();
        INSTANCE.stopRunningServer(options.getPort());
        return INSTANCE.startNewServer(classPath, options.withoutAsyncMode());
    }

    public static <T> T executeCommand(ClientOptions options) throws IOException {
        return (T) executeRawCommand(options);
    }

    public static String executeRawCommand(ClientOptions options) throws IOException {
        return new CommandSender(options).sendRaw(options);
    }

    private String findJarFile() {
        final String classPath = System.getProperty("java.class.path");
        if (classPath.contains(":")) {
            //multiple entries -> not started with java -jar -> search in maven repo
            final String localRepo = findLocalRepo();
            if (localRepo == null) {
                throw new RuntimeException("Could not find local maven repo, try RamlProxy.startServerSync() in a new Thread");
            }
            final String version = Version.VERSION;
            final File jar = new File(localRepo, "guru/nidi/raml/raml-tester-standalone/" + version + "/raml-tester-standalone-" + version + ".jar");
            if (!jar.exists()) {
                throw new RuntimeException("Jar file '" + jar + "' does not exist");
            }
            return jar.getAbsolutePath();
        }
        //one entry -> started with jar -> use this jar
        return classPath;
    }

    private String findLocalRepo() {
        String loc = findLocalRepo(System.getProperty("user.home") + "/.m2");
        if (loc == null || loc.length() == 0) {
            loc = findLocalRepo(System.getenv("M2_HOME") + "/conf");
        }
        return loc;
    }

    private String findLocalRepo(String settingsLocation) {
        final DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        builderFactory.setNamespaceAware(false);
        final DocumentBuilder documentBuilder;
        try {
            documentBuilder = builderFactory.newDocumentBuilder();
            final Document settings = documentBuilder.parse(settingsLocation + "/settings.xml");

            final XPathFactory xPathfactory = XPathFactory.newInstance();
            final XPath xpath = xPathfactory.newXPath();
            final XPathExpression expr = xpath.compile("/settings/localRepository/text()");
            return (String) expr.evaluate(settings, XPathConstants.STRING);
        } catch (Exception e) {
            return null;
        }
    }

    private SubProcess startNewServer(String jar, ServerOptions options) throws IOException, InterruptedException {
        final SubProcess subProcess = new SubProcess(jar, options.asCli());
        String line;
        do {
            line = subProcess.readLine();
            System.out.println(line);
        } while (!line.endsWith("started"));
        return subProcess;
    }

    private void stopRunningServer(int port) {
        try {
            new CommandSender(port).send(Command.STOP);
        } catch (IOException e) {
            //ignore
        }
    }
}
