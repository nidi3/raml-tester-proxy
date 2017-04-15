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

import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

public class SimpleServlet extends HttpServlet implements TomcatServer.ContextIniter {
    @Override
    public void initContext(Context ctx) {
        Tomcat.addServlet(ctx, "app", this);
        ctx.addServletMapping("/*", "app");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        if (req.getPathInfo().startsWith("/resources")) {
            final InputStream in = getClass().getResourceAsStream(req.getPathInfo().substring(11));
            final ServletOutputStream out = res.getOutputStream();
            final byte[] buf = new byte[1000];
            int read;
            while ((read = in.read(buf)) > 0) {
                out.write(buf, 0, read);
            }
            out.flush();
        } else {
            res.setContentType("application/json");
            final PrintWriter out = res.getWriter();
            out.write(req.getParameter("param") == null ? "42" : "illegal json");
            out.flush();
        }
    }
}
