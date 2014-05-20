package guru.nidi.ramlproxy;

import guru.nidi.ramltester.RamlDefinition;
import guru.nidi.ramltester.RamlTester;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import java.io.File;

/**
 *
 */
public class Application {
    public static void main(String[] args) throws Exception {
        LogConfigurer.init();
        final OptionContainer optionContainer = new OptionContainer(args);
        start(optionContainer.getPort(), optionContainer.getTarget());
    }

    private static void start(int port, String target) throws Exception {
        final Server server = new Server(port);
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);
        final RamlDefinition definition = RamlTester
                .fromFile(new File("e:/git/web-controller/src/test/resources/ch/swisscom/cloud/webcontroller"))
                .load("webcontroller-api.raml")
                .assumingServletUri("http://swisscom.ch/cloudportal/v1");
        final ServletHolder servlet = new ServletHolder(new TesterProxyServlet(target, definition));
        servlet.setInitOrder(1);
        context.addServlet(servlet, "/*");
        server.setStopAtShutdown(true);
        server.start();
        server.join();
    }
}
