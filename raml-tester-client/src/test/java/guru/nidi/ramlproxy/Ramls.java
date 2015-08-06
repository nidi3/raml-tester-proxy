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
package guru.nidi.ramlproxy;

import java.io.File;

public class Ramls {
    public static final String MOCK_DIR = clientDir("src/test/resources/guru/nidi/ramlproxy");
    public static final String LOCATION = "file://" + clientDir("src/test/resources/guru/nidi/ramlproxy/"),
            GITHUB = LOCATION + "github-meta.raml",
            SIMPLE = LOCATION + "simple.raml",
            COMMAND = clientDir("src/main/resources/proxy.raml");

    public static String clientDir(String path) {
        if (new File("raml-tester-client").exists()) {
            return "raml-tester-client/" + path;
        }
        return path;
    }
}
