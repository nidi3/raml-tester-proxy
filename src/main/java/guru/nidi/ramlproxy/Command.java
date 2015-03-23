package guru.nidi.ramlproxy;

import guru.nidi.ramltester.core.Usage;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

/**
 *
 */
enum Command {
    PING("ping", Type.TEXT) {
        @Override
        public void execute(TesterFilter testerFilter, BufferedReader reader, PrintWriter writer) {
            writer.write("Pong");
            log("Pong");
        }
    },
    RELOAD("reload", Type.TEXT) {
        @Override
        public void execute(TesterFilter testerFilter, BufferedReader reader, PrintWriter writer) {
            testerFilter.fetchRamlDefinition();
            writer.write("RAML reloaded");
            log("RAML reloaded");
        }
    },
    STOP("stop", Type.TEXT) {
        @Override
        public void execute(final TesterFilter testerFilter, BufferedReader reader, PrintWriter writer) {
            writer.write("Stopping proxy");
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
            try {
                final OptionContainer options = OptionContainer.fromArgs(raw.split(" "));
                writer.write(options.equals(testerFilter.getProxy().getOptions()) ? "same" : "different");
            } catch (ParseException e) {
                writer.println("illegal options: '" + raw + "'");
                e.printStackTrace(writer);
            }
        }
    },
    USAGE("usage", Type.JSON) {
        @Override
        public void execute(TesterFilter testerFilter, BufferedReader reader, PrintWriter writer) throws IOException {
            String res = "";
            for (Map.Entry<String, Usage> usage : testerFilter.getSaver().getAggregator().usages()) {
                final DescribedUsage describedUsage = new DescribedUsage(usage.getValue());
                res += ReportFormat.JSON.formatUsage(usage.getKey(), describedUsage) + ",";
            }
            writer.write(jsonArray(res));
            log("Usage sent");
        }
    },
    REPORTS("reports", Type.JSON) {
        @Override
        public void execute(TesterFilter testerFilter, BufferedReader reader, PrintWriter writer) throws IOException {
            String res = "";
            int id = 0;
            for (ReportSaver.ReportInfo info : testerFilter.getSaver().getReports()) {
                if (!info.getReport().isEmpty()) {
                    res += ReportFormat.JSON.formatViolations(id++, info.getReport(), info.getRequest(), info.getResponse()) + ",";
                }
            }
            writer.write(jsonArray(res));
            log("Reports sent");
        }
    },
    CLEAR_REPORTS("reports/clear", Type.TEXT) {
        @Override
        public void execute(TesterFilter testerFilter, BufferedReader reader, PrintWriter writer) throws IOException {
            testerFilter.getSaver().flushReports();
            writer.write("Reports cleared");
            log("Reports cleared");
        }
    },
    CLEAR_USAGE("usage/clear", Type.TEXT) {
        @Override
        public void execute(TesterFilter testerFilter, BufferedReader reader, PrintWriter writer) throws IOException {
            testerFilter.getSaver().flushUsage();
            writer.write("Usage cleared");
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

    private Command(String name, String type) {
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

    private static void log(String message) {
        log.info(message);
    }

}
