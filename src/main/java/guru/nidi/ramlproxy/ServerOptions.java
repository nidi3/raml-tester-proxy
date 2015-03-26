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

import guru.nidi.ramlproxy.report.ReportFormat;

import java.io.File;

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

    public ServerOptions(int port, String targetOrMockDir, String ramlUri, String baseUri) {
        this(port, target(targetOrMockDir), mockDir(targetOrMockDir), ramlUri, baseUri, null, null, false);
    }

    public ServerOptions(int port, String targetOrMockDir, String ramlUri, String baseUri, File saveDir, ReportFormat fileFormat, boolean ignoreXheaders) {
        this(port, target(targetOrMockDir), mockDir(targetOrMockDir), ramlUri, baseUri, saveDir, fileFormat, ignoreXheaders);
    }

    public ServerOptions(int port, String target, File mockDir, String ramlUri, String baseUri, File saveDir, ReportFormat fileFormat, boolean ignoreXheaders) {
        this.port = port;
        this.target = target;
        this.mockDir = mockDir;
        this.ramlUri = ramlUri;
        this.baseUri = baseUri;
        this.saveDir = saveDir;
        this.fileFormat = fileFormat != null ? fileFormat : ReportFormat.TEXT;
        this.ignoreXheaders = ignoreXheaders;
    }

    private static String target(String targetOrMockDir) {
        return targetOrMockDir.startsWith("http") ? targetOrMockDir : null;
    }

    private static File mockDir(String targetOrMockDir) {
        return targetOrMockDir.startsWith("http") ? null : new File(targetOrMockDir);
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ServerOptions that = (ServerOptions) o;

        if (ignoreXheaders != that.ignoreXheaders) {
            return false;
        }
        if (port != that.port) {
            return false;
        }
        if (baseUri != null ? !baseUri.equals(that.baseUri) : that.baseUri != null) {
            return false;
        }
        if (fileFormat != that.fileFormat) {
            return false;
        }
        if (mockDir != null ? !mockDir.equals(that.mockDir) : that.mockDir != null) {
            return false;
        }
        if (!ramlUri.equals(that.ramlUri)) {
            return false;
        }
        if (saveDir != null ? !saveDir.equals(that.saveDir) : that.saveDir != null) {
            return false;
        }
        if (target != null ? !target.equals(that.target) : that.target != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = port;
        result = 31 * result + (target != null ? target.hashCode() : 0);
        result = 31 * result + (mockDir != null ? mockDir.hashCode() : 0);
        result = 31 * result + ramlUri.hashCode();
        result = 31 * result + (baseUri != null ? baseUri.hashCode() : 0);
        result = 31 * result + (saveDir != null ? saveDir.hashCode() : 0);
        result = 31 * result + fileFormat.hashCode();
        result = 31 * result + (ignoreXheaders ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "OptionContainer{" +
                "port=" + port +
                ", target='" + target + '\'' +
                ", mockDir=" + mockDir +
                ", ramlUri='" + ramlUri + '\'' +
                ", baseUri='" + baseUri + '\'' +
                ", saveDir=" + saveDir +
                ", fileFormat=" + fileFormat +
                ", ignoreXheaders=" + ignoreXheaders +
                '}';
    }

}
