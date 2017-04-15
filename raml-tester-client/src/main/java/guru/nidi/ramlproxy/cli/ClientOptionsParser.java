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

import guru.nidi.ramlproxy.core.ClientOptions;
import guru.nidi.ramlproxy.core.Command;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.util.Locale;

import static org.apache.commons.cli.OptionBuilder.withDescription;

class ClientOptionsParser extends OptionsParser<ClientOptions> {

    @Override
    protected ClientOptions parse(String[] args) throws ParseException {
        final CommandLine cmd = new BasicParser().parse(createOptions(), expandArgs(args));

        final int port = parsePort(cmd);
        final Command command = parseCommand(cmd);
        final boolean clearReports = cmd.hasOption('r');
        final boolean clearUsage = cmd.hasOption('u');
        return new ClientOptions(command, port, clearReports, clearUsage);
    }

    private Command parseCommand(CommandLine cmd) throws ParseException {
        if (cmd.getArgs().length != 1) {
            throw new ParseException("No or multiple commands found: " + cmd.getArgList());
        }
        final String commandStr = cmd.getArgs()[0].toLowerCase(Locale.ENGLISH);
        final Command command = Command.byName(commandStr);
        if (command == null) {
            throw new ParseException("Unknown command '" + commandStr + "'");
        }
        return command;
    }

    @Override
    protected String command() {
        return super.command() + " command";
    }

    @Override
    protected String helpHeader() {
        return "" +
                "Commands:\n" +
                "ping         Ping the proxy\n" +
                "stop         Stop the proxy\n" +
                "reload       Reload the RAML file\n" +
                "reports      Get the reports of the RAML violations\n" +
                "usage        Get information about usage of RAML elements\n" +
                "validate     Get the validation report of the RAML itself\n" +
                "Options:\n";
    }

    @Override
    protected OptionComparator optionComparator() {
        return new OptionComparator("pru");
    }

    @SuppressWarnings("static-access")
    @Override
    protected Options createOptions() {
        return new Options()
                .addOption(withDescription("The port of the proxy\nDefault: " + DEFAULT_PORT).isRequired(false).withArgName("port").hasArg(true).create('p'))
                .addOption(withDescription("Clear the reports").isRequired(false).create('r'))
                .addOption(withDescription("Clear the usage").isRequired(false).create('u'));
    }

}
