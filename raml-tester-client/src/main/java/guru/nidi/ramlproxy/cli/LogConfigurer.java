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
package guru.nidi.ramlproxy.cli;

import org.slf4j.LoggerFactory;
import org.slf4j.impl.SimpleLogger;

final class LogConfigurer {
    private static final String WARN = "warn";
    private static final String INFO = "info";
    private static final String DEBUG = "debug";

    private LogConfigurer() {
    }

    public static void config() {
        System.setProperty(SimpleLogger.DEFAULT_LOG_LEVEL_KEY, DEBUG);
        setLogLevel("org.eclipse.jetty", WARN);
        setLogLevel("org.raml.parser", WARN);
        setLogLevel("org.apache.http", WARN);
        setLogLevel("guru.nidi.ramlproxy.jetty.JettyProxyServlet", INFO);
        LoggerFactory.getILoggerFactory();
    }

    private static void setLogLevel(String prefix, String level) {
        System.setProperty(SimpleLogger.LOG_KEY_PREFIX + prefix, level);
    }

}
