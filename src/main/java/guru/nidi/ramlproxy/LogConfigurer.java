package guru.nidi.ramlproxy;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 */
public class LogConfigurer {
    private static final String FILENAME = "logback.xml";
    private static final Logger log = LoggerFactory.getLogger(LogConfigurer.class);

    public static void init() {
        try {
            final File file = new File(FILENAME);
            if (file.exists()) {
                try (FileInputStream in = new FileInputStream(file)) {
                    init(in);
                }
            }
        } catch (IOException | JoranException e) {
            log.error("Error configuring logger", e);
            throw new RuntimeException(e);
        }
    }

    private static void init(InputStream config) throws JoranException {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        JoranConfigurator configurator = new JoranConfigurator();
        configurator.setContext(context);
        context.reset();
        configurator.doConfigure(config);
    }
}
