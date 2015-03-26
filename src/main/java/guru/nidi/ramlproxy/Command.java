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

import guru.nidi.ramlproxy.report.DescribedUsage;
import guru.nidi.ramlproxy.report.ReportFormat;
import guru.nidi.ramlproxy.report.ReportSaver;
import guru.nidi.ramltester.core.Usage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

/**
 *
 */
public enum Command {
    PING("ping", Type.TEXT) {
        @Override
        public void execute(TesterFilter testerFilter, BufferedReader reader, PrintWriter writer) {
            writer.print("Pong");
            log("Pong");
        }
    },
    RELOAD("reload", Type.TEXT) {
        @Override
        public void execute(TesterFilter testerFilter, BufferedReader reader, PrintWriter writer) {
            testerFilter.fetchRamlDefinition();
            writer.print("RAML reloaded");
            log("RAML reloaded");
        }
    },
    STOP("stop", Type.TEXT) {
        @Override
        public void execute(final TesterFilter testerFilter, BufferedReader reader, PrintWriter writer) {
            writer.print("Stopping proxy");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        log("Stopping proxy");
                        Thread.sleep(100);
                        testerFilter.getProxy().close();
                    } catch (Exception e) {
                        log("Problem stopping proxy, killing instead: " + e);
                        System.exit(1);
                    }
                }
            }).start();
        }
    },
    OPTIONS("options", Type.TEXT) {
        @Override
        public void execute(TesterFilter testerFilter, BufferedReader reader, PrintWriter writer) throws IOException {
            final String raw = reader.readLine();
//            try {
                //TODO
//                final ServerOptions options = ServerOptions.fromArgs(raw.split(" "));
//                writer.print(options.equals(testerFilter.getProxy().getOptions()) ? "same" : "different");
//            } catch (ParseException e) {
//                writer.print("illegal options: '" + raw + "'");
//                e.printStackTrace(writer);
//            }
        }
    },
    USAGE("usage", Type.JSON) {
        @Override
        public void execute(TesterFilter testerFilter, BufferedReader reader, PrintWriter writer) throws IOException {
            String res = "";
            for (Map.Entry<String, Usage> usage : testerFilter.getSaver().getAggregator().usages()) {
                final DescribedUsage describedUsage = new DescribedUsage(usage.getValue());
                res += "\"" + usage.getKey() + "\":" + ReportFormat.JSON.formatUsage(usage.getKey(), describedUsage) + ",";
            }
            writer.print(jsonObject(res));
            log("Usage sent");
        }
    },
    REPORTS("reports", Type.JSON) {
        @Override
        public void execute(TesterFilter testerFilter, BufferedReader reader, PrintWriter writer) throws IOException {
            String res = "";
            int id = 0;
            for (Map.Entry<String, List<ReportSaver.ReportInfo>> infoMap : testerFilter.getSaver().getReports()) {
                res += "\"" + infoMap.getKey() + "\":";
                String list = "";
                for (ReportSaver.ReportInfo info : infoMap.getValue()) {
                    list += ReportFormat.JSON.formatViolations(id++, info.getReport(), info.getRequest(), info.getResponse()) + ",";
                }
                res += jsonArray(list) + ",";
            }
            writer.print(jsonObject(res));
            log("Reports sent");
        }
    },
    CLEAR_REPORTS("reports/clear", Type.TEXT) {
        @Override
        public void execute(TesterFilter testerFilter, BufferedReader reader, PrintWriter writer) throws IOException {
            testerFilter.getSaver().flushReports();
            writer.print("Reports cleared");
            log("Reports cleared");
        }
    },
    CLEAR_USAGE("usage/clear", Type.TEXT) {
        @Override
        public void execute(TesterFilter testerFilter, BufferedReader reader, PrintWriter writer) throws IOException {
            testerFilter.getSaver().flushUsage();
            writer.print("Usage cleared");
            log("Usage cleared");
        }
    };

    private final static Logger log = LoggerFactory.getLogger(Command.class);

    private static final class Type {
        public static final String
                TEXT = "text/plain",
                JSON = "application/json";
    }

    private final String name;
    private final String type;

    Command(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public static Command byName(String name) {
        for (Command command : values()) {
            if (command.name.equals(name)) {
                return command;
            }
        }
        return null;
    }

    public String getName() {
        return name;
    }

    abstract public void execute(TesterFilter testerFilter, BufferedReader reader, PrintWriter writer) throws IOException;

    public void apply(HttpServletResponse response) {
        response.setContentType(type);
    }

    private static String jsonArray(String s) {
        return "[" + (s.length() == 0 ? "" : s.substring(0, s.length() - 1)) + "]";
    }

    private static String jsonObject(String s) {
        return "{" + (s.length() == 0 ? "" : s.substring(0, s.length() - 1)) + "}";
    }

    private static void log(String message) {
        log.info(message);
    }

}
