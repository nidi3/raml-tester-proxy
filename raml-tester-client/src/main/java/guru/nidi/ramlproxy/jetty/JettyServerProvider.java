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
package guru.nidi.ramlproxy.jetty;

import org.eclipse.jetty.server.Server;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class JettyServerProvider {
    private static boolean prestarted = false;
    private static final BlockingQueue<Server> PRESTARTED_SERVER = new ArrayBlockingQueue<>(1);

    public static void prestartServer(final int port) {
        prestarted = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                PRESTARTED_SERVER.add(new Server(port));
            }
        }).start();
    }

    public static Server getServer(int port) throws InterruptedException {
        return prestarted ? PRESTARTED_SERVER.take() : new Server(port);
    }
}
