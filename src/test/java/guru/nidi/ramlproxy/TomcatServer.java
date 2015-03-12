package guru.nidi.ramlproxy;

import org.apache.catalina.*;
import org.apache.catalina.startup.Tomcat;
import org.apache.tomcat.JarScanner;
import org.apache.tomcat.JarScannerCallback;
import org.slf4j.bridge.SLF4JBridgeHandler;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.util.HashSet;
import java.util.Set;

/**
 *
 */
public class TomcatServer implements AutoCloseable {
    public interface ContextIniter {
        void initContext(Context context);
    }

    private final static JarScanner NO_SCAN = new JarScanner() {
        @Override
        public void scan(ServletContext context, ClassLoader classloader, JarScannerCallback callback, Set<String> jarsToSkip) {
        }
    };

    private static Set<Class<?>> inited = new HashSet<>();
    private static Tomcat tomcat;

    private final int port;
    private final ContextIniter contextIniter;

    public TomcatServer(int port, ContextIniter contextIniter) throws ServletException, LifecycleException {
        this.port = port;
        this.contextIniter = contextIniter;
        start();
    }

    private void start() throws LifecycleException, ServletException {
        if (!inited.contains(contextIniter)) {
            inited.add(getClass());
            SLF4JBridgeHandler.removeHandlersForRootLogger();
            SLF4JBridgeHandler.install();

            tomcat = new Tomcat();
            tomcat.setPort(port);
            tomcat.setBaseDir(".");
            Context ctx = tomcat.addWebapp("", "src/test");
            ctx.setJarScanner(NO_SCAN);
            ((Host) ctx.getParent()).setAppBase("");

            contextIniter.initContext(ctx);

            tomcat.start();
            Server server = tomcat.getServer();
            server.start();
        }
    }

    public int getPort() {
        return port;
    }

    public String url() {
        return "http://localhost:" + port;
    }

    @Override
    public void close() throws LifecycleException {
        if (tomcat.getServer() != null && tomcat.getServer().getState() != LifecycleState.DESTROYED) {
            if (tomcat.getServer().getState() != LifecycleState.STOPPED) {
                tomcat.stop();
            }
            tomcat.destroy();
        }
    }

}
