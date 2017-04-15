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
package guru.nidi.ramlproxy.core;

import guru.nidi.ramlproxy.report.ReportSaver;
import guru.nidi.ramltester.RamlDefinition;
import guru.nidi.ramltester.core.RamlReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.BindException;

public abstract class RamlProxyServer implements AutoCloseable {
    private static final Logger log = LoggerFactory.getLogger(RamlProxyServer.class);

    protected final ServerOptions options;
    private final ReportSaver saver;
    private final Thread shutdownHook;

    public RamlProxyServer(ServerOptions options, ReportSaver saver) {
        this.options = options;
        this.saver = saver;
        shutdownHook = shutdownHook(saver);
        Runtime.getRuntime().addShutdownHook(shutdownHook);
    }

    abstract protected void start() throws Exception;

    abstract protected boolean stop() throws Exception;

    abstract public void waitForServer() throws InterruptedException;

    abstract public boolean isStopped();

    protected void doStart() {
        for (int i = 0; i < 10; i++) {
            try {
                start();
                return;
            } catch (BindException e) {
                log.warn("Another server is still running. Retry to start again in 10 ms...");
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e1) {
                    //ignore
                }
            } catch (Exception e) {
                throw new RuntimeException("Problem starting server", e);
            }
        }
        throw new RuntimeException("Another server is still running. Stop it.");
    }

    public RamlDefinition fetchRamlDefinition() {
        return options.fetchRamlDefinition();
    }

    public RamlReport validateRaml(RamlDefinition ramlDefinition) {
        return options.validateRaml(ramlDefinition);
    }

    public void delay() {
        if (options.getMaxDelay() > 0) {
            final int delay = options.getMinDelay() + (int) Math.floor(Math.random() * (1 + options.getMaxDelay() - options.getMinDelay()));
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                //ignore
            }
        }
    }

    public ReportSaver getSaver() {
        return saver;
    }

    @Override
    public void close() throws Exception {
        if (stop()) {
            Runtime.getRuntime().removeShutdownHook(shutdownHook);
            shutdownHook.start();
            shutdownHook.join();
        }
    }

    private static Thread shutdownHook(final ReportSaver saver) {
        final Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                saver.flushUsage();
            }
        });
        thread.setDaemon(true);
        return thread;
    }
}
