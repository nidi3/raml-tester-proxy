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

import guru.nidi.ramlproxy.report.ReportSaver;
import guru.nidi.ramltester.RamlDefinition;
import guru.nidi.ramltester.RamlLoaders;

/**
 *
 */
public abstract class RamlProxyServer implements AutoCloseable {
    protected final ServerOptions options;
    private final ReportSaver saver;
    private final Thread shutdownHook;

    public RamlProxyServer(ServerOptions options, ReportSaver saver) throws Exception {
        this.options = options;
        this.saver = saver;
        shutdownHook = shutdownHook(saver);
        Runtime.getRuntime().addShutdownHook(shutdownHook);
    }

    abstract protected boolean stop() throws Exception;

    abstract public void waitForServer() throws Exception;

    abstract public boolean isStopped();

    public RamlDefinition fetchRamlDefinition() {
        return RamlLoaders.absolutely()
                .load(options.getRamlUri())
                .ignoringXheaders(options.isIgnoreXheaders())
                .assumingBaseUri(options.getBaseOrTargetUri());
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
