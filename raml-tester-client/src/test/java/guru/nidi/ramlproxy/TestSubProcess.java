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
package guru.nidi.ramlproxy;

import guru.nidi.ramlproxy.core.SubProcess;
import org.junit.Assume;

import java.io.*;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestSubProcess extends SubProcess {
    private static final Pattern VERSION = Pattern.compile("<version>(.+?)</version>");

    public TestSubProcess(String... parameters) throws IOException {
        super(findJar(), Arrays.asList(parameters));
    }

    private static String findJar() throws IOException {
        final String jar = "raml-tester-standalone/target/raml-tester-standalone-" + version() + ".jar";
        if (!new File(jar).exists()) {
            Assume.assumeTrue("jar not found", false);
        }
        return jar;
    }

    private static String version() throws IOException {
        String pom = "";
        try (final BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream("pom.xml")))) {
            while (in.ready()) {
                pom += in.readLine();
            }
        }
        final Matcher matcher = VERSION.matcher(pom);
        if (!matcher.find()) {
            throw new IOException("Expected to find " + VERSION);
        }
        if (!matcher.find()) {
            throw new IOException("Expected to find " + VERSION);
        }
        return matcher.group(1);
    }

}
