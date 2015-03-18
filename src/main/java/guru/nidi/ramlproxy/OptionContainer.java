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
    private final int port;
    private final String target;
    private final File mockDir;
    private final String ramlUri;
    private final String baseUri;
    private final File saveDir;
    private final ReportFormat fileFormat;
    private final boolean ignoreXheaders;

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
        this.fileFormat = fileFormat != null ? fileFormat : ReportFormat.TEXT;
        this.ignoreXheaders = ignoreXheaders;
    }

    private OptionContainer(String[] args, Options options) throws ParseException {
        CommandLineParser parser = new BasicParser();
        CommandLine cmd = parser.parse(options, expandArgs(args));
        port = cmd.hasOption('p') ? Integer.parseInt(cmd.getOptionValue('p')) : 8090;
        target = cmd.getOptionValue('t');
        if ((target != null && cmd.hasOption('m')) || (target == null && !cmd.hasOption('m'))) {
            throw new ParseException("Must specify either target (-t) or mock directory (-m)");
        }
        if (cmd.hasOption('m')) {
            if (!cmd.hasOption('b')) {
                throw new ParseException("Missing option -b");
            }
            final String mockDirName = cmd.getOptionValue('m');
            mockDir = (mockDirName == null || mockDirName.length() == 0) ? new File("mock-files") : new File(mockDirName);
        } else {
            mockDir = null;
        }
        ramlUri = cmd.getOptionValue('r');
        baseUri = cmd.getOptionValue('b');
        ignoreXheaders = cmd.hasOption('i');
        final String saveDirName = cmd.getOptionValue('s');
        if (saveDirName != null) {
            saveDir = new File(saveDirName);
            saveDir.mkdirs();
        } else {
            saveDir = null;
        }
        String fileFormatText = cmd.getOptionValue('f');
        fileFormat = fileFormatText != null ? ReportFormat.valueOf(fileFormatText.toUpperCase()) : ReportFormat.TEXT;
    }

    public static OptionContainer fromArgs(String[] args) throws ParseException {
        return new OptionContainer(args, createOptions());
    }

    public static OptionContainer fromCli(String[] args) {
        final Options options = createOptions();
        try {
            return new OptionContainer(args, options);
        } catch (Exception e) {
            handleException(e, options);
            return null;
        }
    }

    private static void handleException(Exception e, Options options) {
        System.out.println(e.getMessage());
        HelpFormatter formatter = new HelpFormatter();
        formatter.setWidth(80);
        formatter.setOptionComparator(OPTION_COMPARATOR);
        formatter.printHelp("java -jar raml-proxy.jar", options);
        System.exit(1);
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
        options.addOption(OptionBuilder.withDescription("Directory with mock files\nDefault: mock-files").isRequired(false).withArgName("directory").hasOptionalArg().create('m'));
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        OptionContainer that = (OptionContainer) o;

        if (ignoreXheaders != that.ignoreXheaders) {
            return false;
        }
        if (port != that.port) {
            return false;
        }
        if (baseUri != null ? !baseUri.equals(that.baseUri) : that.baseUri != null) {
            return false;
        }
        if (fileFormat != that.fileFormat) {
            return false;
        }
        if (mockDir != null ? !mockDir.equals(that.mockDir) : that.mockDir != null) {
            return false;
        }
        if (!ramlUri.equals(that.ramlUri)) {
            return false;
        }
        if (saveDir != null ? !saveDir.equals(that.saveDir) : that.saveDir != null) {
            return false;
        }
        if (target != null ? !target.equals(that.target) : that.target != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = port;
        result = 31 * result + (target != null ? target.hashCode() : 0);
        result = 31 * result + (mockDir != null ? mockDir.hashCode() : 0);
        result = 31 * result + ramlUri.hashCode();
        result = 31 * result + (baseUri != null ? baseUri.hashCode() : 0);
        result = 31 * result + (saveDir != null ? saveDir.hashCode() : 0);
        result = 31 * result + fileFormat.hashCode();
        result = 31 * result + (ignoreXheaders ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "OptionContainer{" +
                "port=" + port +
                ", target='" + target + '\'' +
                ", mockDir=" + mockDir +
                ", ramlUri='" + ramlUri + '\'' +
                ", baseUri='" + baseUri + '\'' +
                ", saveDir=" + saveDir +
                ", fileFormat=" + fileFormat +
                ", ignoreXheaders=" + ignoreXheaders +
                '}';
    }
}
