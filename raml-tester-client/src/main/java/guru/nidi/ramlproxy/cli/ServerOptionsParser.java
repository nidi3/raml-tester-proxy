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

import guru.nidi.ramlproxy.core.ServerOptions;
import guru.nidi.ramlproxy.report.ReportFormat;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.io.File;

import static org.apache.commons.cli.OptionBuilder.withDescription;

/**
 *
 */
class ServerOptionsParser extends OptionsParser<ServerOptions> {
    @Override
    protected ServerOptions parse(String[] args) throws ParseException {
        final CommandLine cmd = new BasicParser().parse(createOptions(), expandArgs(args));

        checkEitherTargetOrMockDir(cmd);

        final int port = parsePort(cmd);
        final String target = cmd.getOptionValue('t');
        final File mockDir = parseMockDir(cmd);
        final String ramlUri = cmd.getOptionValue('r');
        final String baseUri = parseBaseUri(cmd.getOptionValue('b'));
        final boolean ignoreXheaders = cmd.hasOption('i');
        final File saveDir = parseSaveDir(cmd.getOptionValue('s'));
        final ReportFormat fileFormat = parseReportFormat(cmd.getOptionValue('f'));
        final boolean asyncMode = cmd.hasOption('a');
        final int[] delay = parseDelay(cmd.getOptionValue('d'));
        return new ServerOptions(port, target, mockDir, ramlUri, baseUri, saveDir, fileFormat, ignoreXheaders, asyncMode, delay[0], delay[1]);
    }

    private int[] parseDelay(String delay) throws ParseException {
        if (delay == null) {
            return new int[]{0, 0};
        }
        try {
            final int pos = delay.indexOf('-');
            if (pos == 0 || pos == delay.length() - 1) {
                throw new ParseException("Invalid delay format");
            }
            final int max = Integer.parseInt(delay.substring(pos + 1));
            if (pos < 0) {
                return new int[]{max, max};
            }
            final int min = Integer.parseInt(delay.substring(0, pos));
            return new int[]{min, max};
        } catch (NumberFormatException e) {
            throw new ParseException("Invalid number in delay");
        }
    }

    private String parseBaseUri(String baseUri) throws ParseException {
        if (baseUri != null && !baseUri.startsWith("http://") && !baseUri.startsWith("https://")) {
            throw new ParseException("Invalid baseURI: '" + baseUri + "', must start with http:// or https://");
        }
        return baseUri;
    }

    private void checkEitherTargetOrMockDir(CommandLine cmd) throws ParseException {
        final String target = cmd.getOptionValue('t');
        if (target != null && !target.startsWith("http")) {
            throw new ParseException("Target must be an URL");
        }
        if ((target != null && cmd.hasOption('m')) || (target == null && !cmd.hasOption('m'))) {
            throw new ParseException("Must specify either target (-t) or mock directory (-m)");
        }
    }

    private ReportFormat parseReportFormat(String format) {
        return format != null ? ReportFormat.valueOf(format.toUpperCase()) : ReportFormat.TEXT;
    }

    private File parseSaveDir(String saveDirName) {
        if (saveDirName == null) {
            return null;
        }

        final File saveDir = new File(saveDirName);
        saveDir.mkdirs();
        return saveDir;
    }

    private File parseMockDir(CommandLine cmd) throws ParseException {
        if (!cmd.hasOption('m')) {
            return null;
        }
        if (!cmd.hasOption('b')) {
            throw new ParseException("Missing option -b");
        }
        final String mockDirName = cmd.getOptionValue('m');
        return (mockDirName == null || mockDirName.length() == 0) ? new File("mock-files") : new File(mockDirName);
    }

    @Override
    protected OptionComparator optionComparator() {
        return new OptionComparator("rptmbasfid");
    }

    @SuppressWarnings("static-access")
    @Override
    protected Options createOptions() {
        return new Options()
                .addOption(withDescription("Port to listen to\nDefault: " + DEFAULT_PORT).isRequired(false).withArgName("port").hasArg(true).create('p'))
                .addOption(withDescription("Target URL to forward to").isRequired(false).withArgName("URL").hasArg(true).create('t'))
                .addOption(withDescription("Directory with mock files\nDefault: mock-files").isRequired(false).withArgName("directory").hasOptionalArg().create('m'))
                .addOption(withDescription("RAML resource\nFormat: filename,\n[user:pass@]http://, [user:pass@]https://,\n[token@]github://user/project/file, user:pass@apiportal://").isRequired(true).withArgName("URL").hasArg(true).create('r'))
                .addOption(withDescription("Base URI that should be assumed\nDefault: target URL").isRequired(false).withArgName("URI").hasArg(true).create('b'))
                .addOption(withDescription("Save directory for failing requests/responses\nDefault: none").isRequired(false).withArgName("directory").hasArg(true).create('s'))
                .addOption(withDescription("Format to use for report files\nFormat: text|json\nDefault: text").isRequired(false).withArgName("format").hasArg(true).create('f'))
                .addOption(withDescription("Ignore X-headers\nDefault: false").isRequired(false).hasArg(false).create('i'))
                .addOption(withDescription("Asynchronous mode\nDefault: false").isRequired(false).hasArg(false).create('a'))
                .addOption(withDescription("Delay the response (in milliseconds)\nFormat: [minDelay-]maxDelay\nDefault: 0").isRequired(false).withArgName("delay").hasArg(true).create('d'));
    }
}
