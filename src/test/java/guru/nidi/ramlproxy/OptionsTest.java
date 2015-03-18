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

import org.apache.commons.cli.ParseException;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;

/**
 *
 */
public class OptionsTest {
    @Test
    public void minimalTarget() throws ParseException {
        final OptionContainer opt = OptionContainer.fromArgs(new String[]{"-r", "raml", "-t", "http://target"});
        assertEquals(new OptionContainer(8090, "http://target", "raml", null), opt);
    }

    @Test
    public void minimalMock() throws ParseException {
        final OptionContainer opt = OptionContainer.fromArgs(new String[]{"-r", "raml", "-m", "-b", "base"});
        assertEquals(new OptionContainer(8090, "mock-files", "raml", "base"), opt);
    }

    @Test
    public void mockDir() throws ParseException {
        final OptionContainer opt = OptionContainer.fromArgs(new String[]{"-r", "raml", "-m", "mocks", "-b", "base"});
        assertEquals(new OptionContainer(8090, "mocks", "raml", "base"), opt);
    }

    @Test
    public void saveDir() throws ParseException {
        final OptionContainer opt = OptionContainer.fromArgs(new String[]{"-r", "raml", "-t", "http://target", "-s", "save"});
        assertEquals(new OptionContainer(8090, "http://target", "raml", null, new File("save"), null, false), opt);
    }

    @Test
    public void port() throws ParseException {
        final OptionContainer opt = OptionContainer.fromArgs(new String[]{"-r", "raml", "-t", "http://target", "-p", "666"});
        assertEquals(new OptionContainer(666, "http://target", "raml", null), opt);
    }

    @Test
    public void ignoreX() throws ParseException {
        final OptionContainer opt = OptionContainer.fromArgs(new String[]{"-r", "raml", "-t", "http://target", "-i"});
        assertEquals(new OptionContainer(8090, "http://target", "raml", null, null, null, true), opt);
    }

    @Test
    public void formatText() throws ParseException {
        final OptionContainer opt = OptionContainer.fromArgs(new String[]{"-r", "raml", "-t", "http://target", "-f", "text"});
        assertEquals(new OptionContainer(8090, "http://target", "raml", null, null, ReportFormat.TEXT, false), opt);
    }

    @Test
    public void formatJson() throws ParseException {
        final OptionContainer opt = OptionContainer.fromArgs(new String[]{"-r", "raml", "-t", "http://target", "-f", "json"});
        assertEquals(new OptionContainer(8090, "http://target", "raml", null, null, ReportFormat.JSON, false), opt);
    }

    @Test
    public void noSpaceArgs() throws ParseException {
        final OptionContainer opt = OptionContainer.fromArgs(new String[]{"-rraml", "-thttp://target", "-ssave"});
        assertEquals(new OptionContainer(8090, "http://target", "raml", null, new File("save"), null, false), opt);
    }

    @Test(expected = ParseException.class)
    public void neitherTargetNorMock() throws ParseException {
        OptionContainer.fromArgs(new String[]{"-r", "raml"});
    }

    @Test(expected = ParseException.class)
    public void bothTargetAndMock() throws ParseException {
        OptionContainer.fromArgs(new String[]{"-r", "raml", "-t", "http://target", "-m"});
    }

    @Test(expected = ParseException.class)
    public void mockWithoutBase() throws ParseException {
        OptionContainer.fromArgs(new String[]{"-r", "raml", "-m"});
    }
}
