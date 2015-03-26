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

import guru.nidi.ramlproxy.RamlProxy;
import guru.nidi.ramlproxy.ServerOptions;
import guru.nidi.ramlproxy.report.ReportFormat;
import guru.nidi.ramlproxy.report.ReportSaver;
import org.apache.commons.cli.ParseException;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;

import static guru.nidi.ramlproxy.cli.OptionsParser.DEFAULT_PORT;
import static org.junit.Assert.assertEquals;

/**
 *
 */
public class ServerOptionsParserTest {
    private final ServerOptionsParser parser = new ServerOptionsParser();

    @Test
    @Ignore
    public void cors() throws Exception {
        try (final RamlProxy proxy = RamlProxy.create(new ReportSaver(),
                new ServerOptions(8099, "../raml-tester-uc-js/test/data", "file://../raml-tester-uc-js/test/data.raml", "http://raml.nidi.guru"))) {
            proxy.waitForServer();
        }
    }

    @Test
    public void minimalTarget() throws ParseException {
        final ServerOptions opt = parser.fromArgs(new String[]{"-r", "raml", "-t", "http://target"});
        assertEquals(new ServerOptions(DEFAULT_PORT, "http://target", "raml", null), opt);
    }

    @Test
    public void minimalMock() throws ParseException {
        final ServerOptions opt = parser.fromArgs(new String[]{"-r", "raml", "-m", "-b", "http://base"});
        assertEquals(new ServerOptions(DEFAULT_PORT, "mock-files", "raml", "http://base"), opt);
    }

    @Test
    public void mockDir() throws ParseException {
        final ServerOptions opt = parser.fromArgs(new String[]{"-r", "raml", "-m", "mocks", "-b", "http://base"});
        assertEquals(new ServerOptions(DEFAULT_PORT, "mocks", "raml", "http://base"), opt);
    }

    @Test
    public void saveDir() throws ParseException {
        final ServerOptions opt = parser.fromArgs(new String[]{"-r", "raml", "-t", "http://target", "-s", "save"});
        assertEquals(new ServerOptions(DEFAULT_PORT, "http://target", "raml", null, new File("save"), null, false), opt);
    }

    @Test
    public void port() throws ParseException {
        final ServerOptions opt = parser.fromArgs(new String[]{"-r", "raml", "-t", "http://target", "-p", "666"});
        assertEquals(new ServerOptions(666, "http://target", "raml", null), opt);
    }

    @Test
    public void ignoreX() throws ParseException {
        final ServerOptions opt = parser.fromArgs(new String[]{"-r", "raml", "-t", "http://target", "-i"});
        assertEquals(new ServerOptions(DEFAULT_PORT, "http://target", "raml", null, null, null, true), opt);
    }

    @Test
    public void formatText() throws ParseException {
        final ServerOptions opt = parser.fromArgs(new String[]{"-r", "raml", "-t", "http://target", "-f", "text"});
        assertEquals(new ServerOptions(DEFAULT_PORT, "http://target", "raml", null, null, ReportFormat.TEXT, false), opt);
    }

    @Test
    public void formatJson() throws ParseException {
        final ServerOptions opt = parser.fromArgs(new String[]{"-r", "raml", "-t", "http://target", "-f", "json"});
        assertEquals(new ServerOptions(DEFAULT_PORT, "http://target", "raml", null, null, ReportFormat.JSON, false), opt);
    }

    @Test
    public void noSpaceArgs() throws ParseException {
        final ServerOptions opt = parser.fromArgs(new String[]{"-rraml", "-thttp://target", "-ssave"});
        assertEquals(new ServerOptions(DEFAULT_PORT, "http://target", "raml", null, new File("save"), null, false), opt);
    }

    @Test(expected = ParseException.class)
    public void neitherTargetNorMock() throws ParseException {
        parser.fromArgs(new String[]{"-r", "raml"});
    }

    @Test(expected = ParseException.class)
    public void bothTargetAndMock() throws ParseException {
        parser.fromArgs(new String[]{"-r", "raml", "-t", "http://target", "-m"});
    }

    @Test(expected = ParseException.class)
    public void mockWithoutBase() throws ParseException {
        parser.fromArgs(new String[]{"-r", "raml", "-m"});
    }

    @Test(expected = ParseException.class)
    public void wrongBaseUri() throws ParseException {
        final ServerOptions opt = parser.fromArgs(new String[]{"-r", "raml", "-m", "-b", "base"});
        assertEquals(new ServerOptions(DEFAULT_PORT, "mock-files", "raml", "base"), opt);
    }

}
