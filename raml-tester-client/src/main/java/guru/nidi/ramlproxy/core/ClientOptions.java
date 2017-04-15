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

public class ClientOptions {
    private final Command command;
    private final int port;
    private final boolean clearReports;
    private final boolean clearUsage;

    public ClientOptions(Command command, int port) {
        this(command, port, false, false);
    }

    public ClientOptions(Command command, int port, boolean clearReports, boolean clearUsage) {
        this.command = command;
        this.port = port;
        this.clearReports = clearReports;
        this.clearUsage = clearUsage;
    }

    public Command getCommand() {
        return command;
    }

    public int getPort() {
        return port;
    }

    public boolean isClearReports() {
        return clearReports;
    }

    public boolean isClearUsage() {
        return clearUsage;
    }
}
