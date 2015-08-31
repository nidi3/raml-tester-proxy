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

import jdepend.framework.DependencyConstraint;
import jdepend.framework.JDepend;
import jdepend.framework.JavaPackage;
import jdepend.framework.PackageFilter;
import org.junit.Test;

import java.io.IOException;

import static jdepend.framework.DependencyMatchers.*;
import static org.junit.Assert.assertThat;

/**
 *
 */
public class DependencyTest {
    private static final String BASE = "guru.nidi.ramlproxy";

    @Test
    public void dependencies() throws IOException {
        final JDepend jDepend = new JDepend(new PackageFilter() {
            @Override
            public boolean accept(String packageName) {
                return packageName.startsWith(BASE);
            }
        });
        jDepend.addDirectory(Ramls.clientDir("target/classes"));

        DependencyConstraint constraint = new DependencyConstraint();

        final JavaPackage
                base = constraint.addPackage(BASE),
                report = constraint.addPackage(BASE + ".report"),
                cli = constraint.addPackage(BASE + ".cli"),
                jetty = constraint.addPackage(BASE + ".jetty"),
                core = constraint.addPackage(BASE + ".core");

        cli.dependsUpon(core);
        cli.dependsUpon(base);
        cli.dependsUpon(report);

        base.dependsUpon(report);
        base.dependsUpon(core);
        base.dependsUpon(jetty);

        core.dependsUpon(report);

        jetty.dependsUpon(core);
        jetty.dependsUpon(report);

        jDepend.analyze();

        assertThat(jDepend, matches(constraint));
    }

    @Test
    public void circular() throws IOException {
        final JDepend jDepend = new JDepend(new PackageFilter() {
            @Override
            public boolean accept(String packageName) {
                return packageName.startsWith(BASE);
            }
        });
        jDepend.addDirectory(Ramls.clientDir("target/classes"));

        jDepend.analyze();
        assertThat(jDepend, hasNoCycles());

        System.out.println(distances(jDepend, "guru."));
        assertThat(jDepend, hasMaxDistance("guru.", .93));
    }

}
