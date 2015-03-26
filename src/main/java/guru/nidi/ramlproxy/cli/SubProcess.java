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
package guru.nidi.ramlproxy.cli;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 *
 */
class SubProcess implements AutoCloseable {

    private final Process[] proc = new Process[1];
    private final BlockingQueue<String> output = new ArrayBlockingQueue<>(1000);

    public SubProcess(String jarFile, List<String> parameters) throws IOException, InterruptedException {
        final ArrayList<String> params = new ArrayList<>(Arrays.asList("java", "-jar", jarFile));
        params.addAll(parameters);
        proc[0] = new ProcessBuilder(params).redirectErrorStream(true).start();

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
                            Thread.sleep(50);
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

    public void readAllLines() throws InterruptedException {
        while (readLine() != null) {
            ;
        }
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
