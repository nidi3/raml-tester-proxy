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

import guru.nidi.ramlproxy.ServerOptions;
import guru.nidi.ramlproxy.report.ReportFormat;
import org.apache.commons.cli.*;

import java.io.File;

/**
 *
 */
class ServerOptionsParser extends OptionsParser<ServerOptions> {
    @Override
    protected ServerOptions parse(String[] args) throws ParseException {
        CommandLineParser parser = new BasicParser();
        CommandLine cmd = parser.parse(createOptions(), expandArgs(args));
        final int port = cmd.hasOption('p') ? Integer.parseInt(cmd.getOptionValue('p')) : DEFAULT_PORT;
        final String target = cmd.getOptionValue('t');
        if (target != null && !target.startsWith("http")) {
            throw new ParseException("Target must be an URL");
        }
        if ((target != null && cmd.hasOption('m')) || (target == null && !cmd.hasOption('m'))) {
            throw new ParseException("Must specify either target (-t) or mock directory (-m)");
        }
        File mockDir;
        if (cmd.hasOption('m')) {
            if (!cmd.hasOption('b')) {
                throw new ParseException("Missing option -b");
            }
            final String mockDirName = cmd.getOptionValue('m');
            mockDir = (mockDirName == null || mockDirName.length() == 0) ? new File("mock-files") : new File(mockDirName);
        } else {
            mockDir = null;
        }
        final String ramlUri = cmd.getOptionValue('r');
        final String baseUri = cmd.getOptionValue('b');
        final boolean ignoreXheaders = cmd.hasOption('i');
        final String saveDirName = cmd.getOptionValue('s');
        final File saveDir;
        if (saveDirName != null) {
            saveDir = new File(saveDirName);
            saveDir.mkdirs();
        } else {
            saveDir = null;
        }
        String fileFormatText = cmd.getOptionValue('f');
        final ReportFormat fileFormat = fileFormatText != null ? ReportFormat.valueOf(fileFormatText.toUpperCase()) : ReportFormat.TEXT;
        return new ServerOptions(port, target, mockDir, ramlUri, baseUri, saveDir, fileFormat, ignoreXheaders);
    }

    @Override
    protected OptionComparator optionComparator() {
        return new OptionComparator("rptmbsfi");
    }

    @SuppressWarnings("static-access")
    @Override
    protected Options createOptions() {
        final Options options = new Options();
        options.addOption(OptionBuilder.withDescription("The port to listen to\nDefault: " + DEFAULT_PORT).isRequired(false).withArgName("port").hasArg(true).create('p'));
        options.addOption(OptionBuilder.withDescription("The target URL to forward to").isRequired(false).withArgName("URL").hasArg(true).create('t'));
        options.addOption(OptionBuilder.withDescription("Directory with mock files\nDefault: mock-files").isRequired(false).withArgName("directory").hasOptionalArg().create('m'));
        options.addOption(OptionBuilder.withDescription("RAML resource, possible schemas are classpath://, file://, http://, https://").isRequired(true).withArgName("URL").hasArg(true).create('r'));
        options.addOption(OptionBuilder.withDescription("Base URI that should be assumed\nDefault: target URL").isRequired(false).withArgName("URI").hasArg(true).create('b'));
        options.addOption(OptionBuilder.withDescription("Save directory for failing requests/responses\nDefault: none").isRequired(false).withArgName("directory").hasArg(true).create('s'));
        options.addOption(OptionBuilder.withDescription("Format to use for report files, either text or json\nDefault: text").isRequired(false).withArgName("format").hasArg(true).create('f'));
        options.addOption(OptionBuilder.withDescription("Ignore X-headers\nDefault: false").isRequired(false).hasArg(false).create('i'));
        return options;
    }

}
