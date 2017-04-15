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

import guru.nidi.ramlproxy.core.*;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class HighlevelTest {
    @Test
    public void async() {
        try (final SubProcess proxy = RamlProxy.startServerAsync(new ServerOptions(8099, Ramls.MOCK_DIR, Ramls.SIMPLE, "http://nidi.guru/raml", null, null, true))) {
            final HttpSender sender = new HttpSender(8099);
            assertEquals("42", sender.contentOfGet("v1/data"));
        } catch (Exception e) {
            //fails if jar has not yet been installed in maven repo, ignore
        }
    }

    @Test
    public void sync() throws Exception {
        try (final RamlProxyServer proxy = RamlProxy.startServerSync(new ServerOptions(8099, Ramls.MOCK_DIR, Ramls.SIMPLE, "http://nidi.guru/raml", null, null, true))) {
            final HttpSender sender = new HttpSender(8099);
            assertEquals("42", sender.contentOfGet("v1/data"));
        }
    }

    @Test
    public void command() throws Exception {
        try (final RamlProxyServer proxy = RamlProxy.startServerSync(new ServerOptions(8099, Ramls.MOCK_DIR, Ramls.SIMPLE, "http://nidi.guru/raml", null, null, true))) {
            assertEquals("Pong", RamlProxy.executeCommand(new ClientOptions(Command.PING, 8099)));
        }
    }
}
