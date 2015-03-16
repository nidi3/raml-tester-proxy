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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 *
 */
public class OptionContainer {
    private static final Comparator<Option> OPTION_COMPARATOR = new Comparator<Option>() {
        private String OPTIONS = "ptmrbsfi";

        @Override
        public int compare(Option o1, Option o2) {
            return OPTIONS.indexOf(o1.getOpt().charAt(0)) - OPTIONS.indexOf(o2.getOpt().charAt(0));
        }
    };
    private int port;
    private String target;
    private File mockDir;
    private String ramlUri;
    private String baseUri;
    private File saveDir;
    private ReportFormat fileFormat;
    private boolean ignoreXheaders;

    public OptionContainer(int port, String targetOrMockDir, String ramlUri, String baseUri) {
        this(port, targetOrMockDir, ramlUri, baseUri, null, null, false);
    }

    public OptionContainer(int port, String targetOrMockDir, String ramlUri, String baseUri, File saveDir, ReportFormat fileFormat, boolean ignoreXheaders) {
        this.port = port;
        this.target = targetOrMockDir.startsWith("http") ? targetOrMockDir : null;
        this.mockDir = targetOrMockDir.startsWith("http") ? null : new File(targetOrMockDir);
        this.ramlUri = ramlUri;
        this.baseUri = baseUri;
        this.saveDir = saveDir;
        this.fileFormat = fileFormat;
        this.ignoreXheaders = ignoreXheaders;
    }

    public OptionContainer(String[] args) {
        final Options options = createOptions();
        try {
            CommandLineParser parser = new BasicParser();
            CommandLine cmd = parser.parse(options, expandArgs(args));
            port = cmd.hasOption('p') ? Integer.parseInt(cmd.getOptionValue('p')) : 8090;
            target = cmd.getOptionValue('t');
            if ((target != null && cmd.hasOption('m')) || (target == null && !cmd.hasOption('m'))) {
                throw new Exception("Must specify either target (-t) or mock directory (-m)");
            }
            if (cmd.hasOption('m')) {
                if (!cmd.hasOption('b')) {
                    throw new Exception("Missing option -b");
                }
                final String mockDirName = cmd.getOptionValue('m');
                mockDir = (mockDirName == null || mockDirName.length() == 0) ? new File("mock-files") : new File(mockDirName);
            }
            ramlUri = cmd.getOptionValue('r');
            baseUri = cmd.getOptionValue('b');
            ignoreXheaders = cmd.hasOption('i');
            final String saveDirName = cmd.getOptionValue('s');
            if (saveDirName != null) {
                saveDir = new File(saveDirName);
                saveDir.mkdirs();
            }
            String fileFormatText = cmd.getOptionValue('f');
            fileFormat = fileFormatText != null ? ReportFormat.valueOf(fileFormatText.toUpperCase()) : ReportFormat.TEXT;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            HelpFormatter formatter = new HelpFormatter();
            formatter.setWidth(80);
            formatter.setOptionComparator(OPTION_COMPARATOR);
            formatter.printHelp("java -jar raml-proxy.jar", options);
            System.exit(1);
        }
    }

    private String[] expandArgs(String[] args) {
        final List<String> res = new ArrayList<>();
        for (String arg : args) {
            if (arg.charAt(0) == '-' && arg.length() > 2) {
                res.add(arg.substring(0, 2));
                res.add(arg.substring(2));
            } else {
                res.add(arg);
            }
        }
        return res.toArray(new String[res.size()]);
    }

    @SuppressWarnings("static-access")
    private static Options createOptions() {
        final Options options = new Options();
        options.addOption(OptionBuilder.withDescription("The port to listen to\nDefault: 8090").isRequired(false).withArgName("port").hasArg(true).create('p'));
        options.addOption(OptionBuilder.withDescription("The target URL to forward to").isRequired(false).withArgName("URL").hasArg(true).create('t'));
        options.addOption(OptionBuilder.withDescription("Directory with mock files\nDefault: mock-files").isRequired(false).withArgName("directory").hasArg(false).create('m'));
        options.addOption(OptionBuilder.withDescription("RAML resource, possible schemas are classpath://, file://, http://, https://").isRequired(true).withArgName("URL").hasArg(true).create('r'));
        options.addOption(OptionBuilder.withDescription("Base URI that should be assumed\nDefault: target URL").isRequired(false).withArgName("URI").hasArg(true).create('b'));
        options.addOption(OptionBuilder.withDescription("Save directory for failing requests/responses\nDefault: none").isRequired(false).withArgName("directory").hasArg(true).create('s'));
        options.addOption(OptionBuilder.withDescription("Format to use for report files, either text or json\nDefault: text").isRequired(false).withArgName("format").hasArg(true).create('f'));
        options.addOption(OptionBuilder.withDescription("Ignore X-headers\nDefault: false").isRequired(false).hasArg(false).create('i'));
        return options;
    }

    public int getPort() {
        return port;
    }

    public String getTarget() {
        return target;
    }

    public String getTargetUrl() {
        return target.startsWith("http") ? target : ("http://" + target);
    }

    public boolean isMockMode() {
        return mockDir != null;
    }

    public File getMockDir() {
        return mockDir;
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

    public String getBaseOrTargetUri() {
        return getBaseUri() != null ? getBaseUri() : getTargetUrl();
    }

    public ReportFormat getFileFormat() {
        return fileFormat;
    }

    public boolean isIgnoreXheaders() {
        return ignoreXheaders;
    }
}
