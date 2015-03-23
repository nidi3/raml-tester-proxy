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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class MockServlet extends HttpServlet {
    private final static Logger log = LoggerFactory.getLogger(MockServlet.class);
    private final static Map<String, String> EXTENSION_MIME_TYPE = new HashMap<String, String>() {{
        put("json", "application/json");
        put("xml", "application/xml");
        put("txt", "text/plain");
    }};

    private final ObjectMapper mapper = new ObjectMapper();
    private final File mockDir;

    public MockServlet(File mockDir) {
        this.mockDir = mockDir;
    }

    @Override
    public void init() throws ServletException {
        super.init();
        log.info("Mock started");
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        final String pathInfo = req.getPathInfo();
        final int pos = pathInfo.lastIndexOf('/');
        final String path = pathInfo.substring(1, pos + 1);
        final String name = pathInfo.substring(pos + 1);
        final ServletOutputStream out = res.getOutputStream();
        final File targetDir = new File(mockDir, path);
        final File file = findFileOrParent(targetDir, name, req.getMethod());
        if (file == null) {
            res.sendError(HttpServletResponse.SC_NOT_FOUND, "No or multiple file '" + name + "' found in directory '" + targetDir.getAbsolutePath() + "'");
            return;
        }
        handleMeta(req, res, file.getParentFile(), file.getName());
        res.setContentLength((int) file.length());
        res.setContentType(mineType(file));
        res.setHeader("Access-Control-Allow-Origin", "*");
        try (final InputStream in = new FileInputStream(file)) {
            copy(in, out);
        } catch (IOException e) {
            res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Problem delivering file '" + file.getAbsolutePath() + "': " + e);
        }
        out.flush();
    }

    private File findFileOrParent(File targetDir, String name, String method) {
        File file = findFile(targetDir, name, method);
        while (file == null && !targetDir.equals(mockDir.getParentFile())) {
            file = findFile(targetDir, "RESPONSE", method);
            targetDir = targetDir.getParentFile();
        }
        return file;
    }

    private void handleMeta(HttpServletRequest req, HttpServletResponse res, File targetDir, String name) throws IOException {
        final int dotPos = name.lastIndexOf('.');
        if (dotPos > 0) {
            name = name.substring(0, dotPos);
        }
        final File metaFile = findFile(targetDir, "META-" + name, req.getMethod());
        if (metaFile != null) {
            try {
                final ReponseMetaData meta = new ReponseMetaData(mapper.readValue(metaFile, Map.class));
                meta.apply(res);
            } catch (Exception e) {
                log.warn("Problem applying meta data for '" + targetDir + "/" + name + "': " + e);
            }
        }
    }

    private void copy(InputStream in, OutputStream out) throws IOException {
        final byte[] buf = new byte[4096];
        int read;
        while ((read = in.read(buf)) > 0) {
            out.write(buf, 0, read);
        }
    }

    private File findFile(File dir, String name, String method) {
        final File methodFile = findFile(dir, method + "-" + name);
        final File generalFile = findFile(dir, name);
        return methodFile != null ? methodFile : generalFile;
    }

    private File findFile(File dir, final String name) {
        final File[] files = dir.listFiles();
        if (files == null) {
            return null;
        }
        File res = null;
        for (File file : files) {
            final String fileName = file.getName();
            final int dotPos = fileName.lastIndexOf('.');
            if (dotPos > 0 && fileName.substring(0, dotPos).equals(name)) {
                if (EXTENSION_MIME_TYPE.containsKey(fileName.substring(dotPos + 1))) {
                    if (res == null) {
                        res = file;
                    } else {
                        return null;
                    }
                }
            }
        }
        return res;
    }

    private String mineType(File file) {
        final String fileName = file.getName();
        final int dotPos = fileName.lastIndexOf('.');
        return EXTENSION_MIME_TYPE.get(fileName.substring(dotPos + 1));
    }

}


