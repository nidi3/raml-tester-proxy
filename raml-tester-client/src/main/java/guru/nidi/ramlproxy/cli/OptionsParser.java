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

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
abstract class OptionsParser<T> {
    static final int DEFAULT_PORT = 8099;

    abstract protected Options createOptions();

    abstract protected OptionComparator optionComparator();

    abstract protected T parse(String[] args) throws ParseException;

    protected String command() {
        return "java -jar raml-tester-standalone.jar";
    }

    protected String helpHeader() {
        return "";
    }

    public T fromArgs(String[] args) throws ParseException {
        return parse(args);
    }

    public T fromCli(String[] args) {
        try {
            return parse(args);
        } catch (Exception e) {
            handleException(e);
            return null;
        }
    }

    protected void handleException(Exception e) {
        System.out.println(e.getMessage());
        showHelp();
        System.exit(1);
    }

    public void showHelp() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.setWidth(80);
        formatter.setOptionComparator(optionComparator());
        formatter.printHelp(command(), helpHeader(), createOptions(), "", true);
    }

    protected String[] expandArgs(String[] args) {
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

    protected int parsePort(CommandLine cmd) {
        return cmd.hasOption('p') ? Integer.parseInt(cmd.getOptionValue('p')) : DEFAULT_PORT;
    }

}
