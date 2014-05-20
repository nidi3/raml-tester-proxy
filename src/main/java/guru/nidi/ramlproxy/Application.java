package guru.nidi.ramlproxy;

import guru.nidi.ramltester.RamlDefinition;
import guru.nidi.ramltester.RamlTester;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class Application {
    private final static Logger log = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) throws Exception {
        LogConfigurer.init();
        final OptionContainer optionContainer = new OptionContainer(args);
        start(optionContainer);
    }

    private static void start(OptionContainer options) throws Exception {
        final Server server = new Server(options.getPort());
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);
        final RamlDefinition definition = RamlTester
                .loadFromUri(options.getRamlUri())
                .assumingServletUri(options.getBaseUri());
        final ServletHolder servlet = new ServletHolder(new TesterProxyServlet(options.getTarget(), definition, options.getSaveDir()));
        servlet.setInitOrder(1);
        context.addServlet(servlet, "/*");
        server.setStopAtShutdown(true);
        server.start();
        log.info("Proxy started");
        server.join();
    }
}
