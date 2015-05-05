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
package guru.nidi.ramlproxy.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import guru.nidi.ramlproxy.report.*;
import guru.nidi.ramltester.core.Usage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 */
public enum Command {
    PING("ping", Type.TEXT) {
        @Override
        public void execute(CommandContext context, PrintWriter out) {
            out.print("Pong");
            log("Pong");
        }

        @Override
        public String decode(String response) throws IOException {
            return asserted(response, "Pong");
        }

    },
    RELOAD("reload", Type.TEXT) {
        @Override
        public void execute(CommandContext context, PrintWriter out) {
            context.reloadRamlDefinition();
            out.print("RAML reloaded");
            log("RAML reloaded");
        }

        @Override
        public String decode(String response) throws IOException {
            return asserted(response, "RAML reloaded");
        }
    },
    STOP("stop", Type.TEXT) {
        @Override
        public void execute(final CommandContext context, PrintWriter out) {
            out.print("Stopping proxy");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        log("Stopping proxy");
                        Thread.sleep(10);
                        context.stopProxy();
                    } catch (Exception e) {
                        log("Problem stopping proxy, killing instead: " + e);
                        System.exit(1);
                    }
                }
            }).start();
        }

        @Override
        public String decode(String response) throws IOException {
            return "";
        }
    },
    USAGE("usage", Type.JSON) {
        private final ObjectMapper MAPPER = new ObjectMapper();

        @Override
        public void execute(CommandContext context, PrintWriter out) throws IOException {
            final UsageDatas res = new UsageDatas();
            for (Map.Entry<String, Usage> usage : context.getSaver().getAggregator().usages()) {
                res.put(usage.getKey(), ReportFormat.createUsageData(usage.getKey(), usage.getValue()));
            }
            out.print(MAPPER.writeValueAsString(res));
            log("Usage sent");
        }

        @Override
        public UsageDatas decode(String response) throws IOException {
            return MAPPER.readValue(response, UsageDatas.class);
        }
    },
    REPORTS("reports", Type.JSON) {
        private final ObjectMapper MAPPER = new ObjectMapper();

        @Override
        public void execute(CommandContext context, PrintWriter out) throws IOException {
            final ViolationDatas res = new ViolationDatas();
            int id = 0;
            for (Map.Entry<String, List<ReportSaver.ReportInfo>> infoMap : context.getSaver().getReports()) {
                final List<ViolationData> data = new ArrayList<>();
                for (ReportSaver.ReportInfo info : infoMap.getValue()) {
                    data.add(ReportFormat.createViolationData(id++, info.getReport(), info.getRequest(), info.getResponse()));
                }
                res.put(infoMap.getKey(), data);
            }
            out.print(MAPPER.writeValueAsString(res));
            log("Reports sent");
        }

        @Override
        public ViolationDatas decode(String response) throws IOException {
            return MAPPER.readValue(response, ViolationDatas.class);
        }
    },
    CLEAR_REPORTS("reports/clear", Type.TEXT) {
        @Override
        public void execute(CommandContext context, PrintWriter out) throws IOException {
            context.getSaver().flushReports();
            log("Reports cleared");
        }

        @Override
        public String decode(String response) throws IOException {
            return "";
        }
    },
    CLEAR_USAGE("usage/clear", Type.TEXT) {
        @Override
        public void execute(CommandContext context, PrintWriter out) throws IOException {
            context.getSaver().flushUsage();
            log("Usage cleared");
        }

        @Override
        public String decode(String response) throws IOException {
            return "";
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

    abstract public void execute(CommandContext context, PrintWriter out) throws IOException;

    abstract public Object decode(String response) throws IOException;

    public void apply(HttpServletResponse response) {
        response.setContentType(type);
    }

    private static void log(String message) {
        log.info(message);
    }

    private static String asserted(String content, String expected) throws IOException {
        if (!content.equals(expected)) {
            throw new IOException("Unexpected response: '" + content + "'");
        }
        return content;
    }

}
