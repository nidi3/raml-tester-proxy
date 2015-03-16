package guru.nidi.ramlproxy;

import org.junit.Assume;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 */
public class ProxyProcess implements AutoCloseable {
    private static final Pattern VERSION = Pattern.compile("<version>(.+?)</version>");

    private static String version() throws IOException {
        String pom = "";
        try (final BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream("pom.xml")))) {
            while (in.ready()) {
                pom += in.readLine();
            }
        }
        final Matcher matcher = VERSION.matcher(pom);
        matcher.find();
        matcher.find();
        return matcher.group(1);
    }

    private final Process[] proc = new Process[1];
    private final BlockingQueue<String> output = new ArrayBlockingQueue<>(1000);

    public ProxyProcess(String... parameters) throws IOException, InterruptedException {
        final String jar = "target/raml-tester-proxy-" + version() + ".jar";
        if (!new File(jar).exists()) {
            Assume.assumeTrue("jar not found", false);
            return;
        }
        final ArrayList<String> params = new ArrayList<>(Arrays.asList("java", "-jar", jar));
        params.addAll(Arrays.asList(parameters));
        proc[0] = new ProcessBuilder(params).start();

        final Thread reader = new Thread(new Runnable() {
            @Override
            public void run() {
                final BufferedReader in = new BufferedReader(new InputStreamReader(proc[0].getInputStream()));
                for (; ; ) {
                    try {
                        String line;
                        if (in.ready() && (line = in.readLine()) != null) {
                            output.add(line);
                        } else {
                            Thread.sleep(100);
                        }
                    } catch (Exception e) {
                    }
                }
            }
        });
        reader.setDaemon(true);
        reader.start();
    }

    @Override
    public void close() {
        if (proc[0] != null) {
            proc[0].destroy();
        }
    }

    public String readLine() throws InterruptedException {
        return readLine(1);
    }

    public String readLine(int maxWaitSec) throws InterruptedException {
        return output.poll(maxWaitSec, TimeUnit.SECONDS);
    }

    public boolean hasEnded() {
        try {
            proc[0].exitValue();
            return true;
        } catch (IllegalThreadStateException e) {
            return false;
        }
    }
}
