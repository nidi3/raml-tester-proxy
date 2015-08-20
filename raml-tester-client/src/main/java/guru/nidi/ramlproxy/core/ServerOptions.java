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

import guru.nidi.ramlproxy.report.ReportFormat;
import guru.nidi.ramltester.RamlDefinition;
import guru.nidi.ramltester.RamlLoaders;
import guru.nidi.ramltester.core.RamlReport;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 *
 */
public class ServerOptions {
    private final int port;
    private final String target;
    private final File mockDir;
    private final String ramlUri;
    private final String baseUri;
    private final File saveDir;
    private final ReportFormat fileFormat;
    private final boolean ignoreXheaders;
    private final boolean asyncMode;
    private final int minDelay, maxDelay;
    private final ValidatorConfigurator validatorConfigurator;

    public ServerOptions(int port, String targetOrMockDir, String ramlUri, String baseUri) {
        this(port, target(targetOrMockDir), mockDir(targetOrMockDir), ramlUri, baseUri, null, null, false, false, 0, 0, ValidatorConfigurator.DEFAULT);
    }

    public ServerOptions(int port, String targetOrMockDir, String ramlUri, String baseUri, File saveDir, ReportFormat fileFormat, boolean ignoreXheaders) {
        this(port, target(targetOrMockDir), mockDir(targetOrMockDir), ramlUri, baseUri, saveDir, fileFormat, ignoreXheaders, false, 0, 0, ValidatorConfigurator.DEFAULT);
    }

    public ServerOptions(int port, String target, File mockDir, String ramlUri, String baseUri, File saveDir, ReportFormat fileFormat, boolean ignoreXheaders, boolean asyncMode, int minDelay, int maxDelay, ValidatorConfigurator validatorConfigurator) {
        this.port = port;
        this.target = target;
        this.mockDir = mockDir;
        this.ramlUri = ramlUri;
        this.baseUri = baseUri;
        this.saveDir = saveDir;
        this.fileFormat = fileFormat != null ? fileFormat : ReportFormat.TEXT;
        this.ignoreXheaders = ignoreXheaders;
        this.asyncMode = asyncMode;
        this.minDelay = minDelay;
        this.maxDelay = maxDelay;
        this.validatorConfigurator = validatorConfigurator;
    }

    public ServerOptions withoutAsyncMode() {
        return new ServerOptions(port, target, mockDir, ramlUri, baseUri, saveDir, fileFormat, ignoreXheaders, false, minDelay, maxDelay, validatorConfigurator);
    }

    private static String target(String targetOrMockDir) {
        return targetOrMockDir.startsWith("http") ? targetOrMockDir : null;
    }

    private static File mockDir(String targetOrMockDir) {
        return targetOrMockDir.startsWith("http") ? null : new File(targetOrMockDir);
    }

    public List<String> asCli() {
        final String args = "" +
                ("-p" + port) +
                (target != null ? (" -t" + target) : "") +
                (mockDir != null ? (" -m" + mockDir.getAbsolutePath()) : "") +
                (" -r" + ramlUri) +
                (baseUri != null ? (" -b" + baseUri) : "") +
                (saveDir != null ? (" -s" + saveDir) : "") +
                (fileFormat != null ? (" -f" + fileFormat) : "") +
                (ignoreXheaders ? " -i" : "") +
                (asyncMode ? " -a" : "") +
                (" -d" + minDelay + "-" + maxDelay) +
                (" " + validatorConfigurator.asCli());
        return Arrays.asList(args.split(" "));
    }

    public int getPort() {
        return port;
    }

    public String getTarget() {
        return target;
    }

    public String getTargetUrl() {
        return target.startsWith("http") ? target : ("http://" + target);
    }

    public boolean isMockMode() {
        return mockDir != null;
    }

    public File getMockDir() {
        return mockDir;
    }

    public String getRamlUri() {
        return ramlUri;
    }

    public File getSaveDir() {
        return saveDir;
    }

    public String getBaseUri() {
        return baseUri;
    }

    public String getBaseOrTargetUri() {
        return getBaseUri() != null ? getBaseUri() : getTargetUrl();
    }

    public ReportFormat getFileFormat() {
        return fileFormat;
    }

    public boolean isIgnoreXheaders() {
        return ignoreXheaders;
    }

    public boolean isAsyncMode() {
        return asyncMode;
    }

    public int getMinDelay() {
        return minDelay;
    }

    public int getMaxDelay() {
        return maxDelay;
    }

    public ValidatorConfigurator getValidatorConfigurator() {
        return validatorConfigurator;
    }

    public RamlDefinition fetchRamlDefinition() {
        return RamlLoaders.fromFile(".")
                .load(getRamlUri())
                .ignoringXheaders(isIgnoreXheaders())
                .assumingBaseUri(getBaseOrTargetUri());
    }

    public RamlReport validateRaml(RamlDefinition ramlDefinition) {
        return getValidatorConfigurator().configure(ramlDefinition.validator()).validate();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ServerOptions that = (ServerOptions) o;

        if (port != that.port) {
            return false;
        }
        if (ignoreXheaders != that.ignoreXheaders) {
            return false;
        }
        if (asyncMode != that.asyncMode) {
            return false;
        }
        if (target != null ? !target.equals(that.target) : that.target != null) {
            return false;
        }
        if (mockDir != null ? !mockDir.equals(that.mockDir) : that.mockDir != null) {
            return false;
        }
        if (ramlUri != null ? !ramlUri.equals(that.ramlUri) : that.ramlUri != null) {
            return false;
        }
        if (baseUri != null ? !baseUri.equals(that.baseUri) : that.baseUri != null) {
            return false;
        }
        if (saveDir != null ? !saveDir.equals(that.saveDir) : that.saveDir != null) {
            return false;
        }
        if (minDelay != that.minDelay) {
            return false;
        }
        if (maxDelay != that.maxDelay) {
            return false;
        }
        return fileFormat == that.fileFormat;

    }

    @Override
    public int hashCode() {
        int result = port;
        result = 31 * result + (target != null ? target.hashCode() : 0);
        result = 31 * result + (mockDir != null ? mockDir.hashCode() : 0);
        result = 31 * result + (ramlUri != null ? ramlUri.hashCode() : 0);
        result = 31 * result + (baseUri != null ? baseUri.hashCode() : 0);
        result = 31 * result + (saveDir != null ? saveDir.hashCode() : 0);
        result = 31 * result + (fileFormat != null ? fileFormat.hashCode() : 0);
        result = 31 * result + (ignoreXheaders ? 1 : 0);
        result = 31 * result + (asyncMode ? 1 : 0);
        result = 31 * result + minDelay;
        result = 31 * result + maxDelay;
        return result;
    }

    @Override
    public String toString() {
        return "ServerOptions{" +
                "port=" + port +
                ", target='" + target + '\'' +
                ", mockDir=" + mockDir +
                ", ramlUri='" + ramlUri + '\'' +
                ", baseUri='" + baseUri + '\'' +
                ", saveDir=" + saveDir +
                ", fileFormat=" + fileFormat +
                ", ignoreXheaders=" + ignoreXheaders +
                ", asyncMode=" + asyncMode +
                ", minDelay=" + minDelay +
                ", maxDelay=" + maxDelay +
                '}';
    }
}
