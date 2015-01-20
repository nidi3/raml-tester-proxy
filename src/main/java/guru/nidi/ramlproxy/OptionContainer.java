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

import org.apache.commons.cli.*;

import java.io.File;

/**
 *
 */
public class OptionContainer {
    private int port;
    private String target;
    private String ramlUri;
    private String baseUri;
    private File saveDir;
    private ReportFormat fileFormat;

    public OptionContainer(int port, String target, String ramlUri, String baseUri) {
        this(port, target, ramlUri, baseUri, null, null);
    }

    public OptionContainer(int port, String target, String ramlUri, String baseUri, File saveDir, ReportFormat fileFormat) {
        this.port = port;
        this.target = target;
        this.ramlUri = ramlUri;
        this.baseUri = baseUri;
        this.saveDir = saveDir;
        this.fileFormat = fileFormat;
    }

    public OptionContainer(String[] args) {
        final Options options = createOptions();
        try {
            CommandLineParser parser = new BasicParser();
            CommandLine cmd = parser.parse(options, args);
            port = Integer.parseInt(cmd.getOptionValue('p'));
            target = cmd.getOptionValue('t');
            ramlUri = cmd.getOptionValue('r');
            baseUri = cmd.getOptionValue('b');
            String saveDirName = cmd.getOptionValue('s');
            if (saveDirName != null) {
                saveDir = new File(saveDirName);
                saveDir.mkdirs();
            }
            String fileFormatText = cmd.getOptionValue('f');
            fileFormat = fileFormatText != null ? ReportFormat.valueOf(fileFormatText.toUpperCase()) : ReportFormat.TEXT;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("java -jar raml-proxy.jar", options);
            System.exit(1);
        }
    }

    @SuppressWarnings("static-access")
    private static Options createOptions() {
        final Options options = new Options();
        options.addOption(OptionBuilder.withDescription("The port to listen to").isRequired(true).withArgName("port").hasArg(true).create('p'));
        options.addOption(OptionBuilder.withDescription("The target uri to forward to").isRequired(true).withArgName("URL").hasArg(true).create('t'));
        options.addOption(OptionBuilder.withDescription("RAML resource, possible schemas are classpath://, file://, http://, https://").isRequired(true).withArgName("URL").hasArg(true).create('r'));
        options.addOption(OptionBuilder.withDescription("Base URI that should be assumed").isRequired(false).withArgName("URI").hasArg(true).create('b'));
        options.addOption(OptionBuilder.withDescription("Save directory for failing requests/responses").isRequired(false).withArgName("directory").hasArg(true).create('s'));
        options.addOption(OptionBuilder.withDescription("Format to use for report files, either text or json (defaults to text)").isRequired(false).withArgName("format").hasArg(true).create('f'));
        return options;
    }

    public int getPort() {
        return port;
    }

    public String getTarget() {
        return target;
    }

    public String getRamlUri() {
        return ramlUri;
    }

    public File getSaveDir() {
        return saveDir;
    }

    public String getBaseUri() {
        return baseUri;
    }

    public ReportFormat getFileFormat() {
        return fileFormat;
    }
}
