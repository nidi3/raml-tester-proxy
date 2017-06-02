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

import guru.nidi.codeassert.config.AnalyzerConfig;
import guru.nidi.codeassert.config.In;
import guru.nidi.codeassert.dependency.DependencyRule;
import guru.nidi.codeassert.dependency.DependencyRuler;
import guru.nidi.codeassert.dependency.DependencyRules;
import guru.nidi.codeassert.findbugs.BugCollector;
import guru.nidi.codeassert.findbugs.FindBugsAnalyzer;
import guru.nidi.codeassert.findbugs.FindBugsResult;
import guru.nidi.codeassert.junit.CodeAssertTest;
import guru.nidi.codeassert.model.ModelAnalyzer;
import guru.nidi.codeassert.model.ModelResult;
import guru.nidi.codeassert.pmd.*;
import guru.nidi.ramlproxy.core.Command;
import org.junit.Test;

import static guru.nidi.codeassert.junit.CodeAssertMatchers.packagesMatchExactly;
import static guru.nidi.codeassert.pmd.Rulesets.*;
import static org.junit.Assert.assertThat;

public class CodeTest extends CodeAssertTest {
    private static final AnalyzerConfig config = AnalyzerConfig.maven("raml-tester-client").main();

    @Test
    public void dependencies() {
        class GuruNidiRamlproxy extends DependencyRuler {
            DependencyRule report, cli, jetty, core, data;

            @Override
            public void defineRules() {
                cli.mayUse(core, report, base());
                base().mayUse(core, report, jetty);
                jetty.mayUse(core, report);
                core.mayUse(report, data);
                report.mayUse(data);
            }
        }

        final DependencyRules rules = DependencyRules.denyAll()
                .withExternals("java*", "org*", "com*", "guru.nidi.ramltester*")
                .withRelativeRules(new GuruNidiRamlproxy());
        assertThat(modelResult(), packagesMatchExactly(rules));
    }


    @Override
    protected ModelResult analyzeModel() {
        return new ModelAnalyzer(config).analyze();
    }

    @Override
    protected FindBugsResult analyzeFindBugs() {
        return new FindBugsAnalyzer(config, new BugCollector()
//                .just(In.everywhere().ignore(
//                        "SE_NO_SERIALVERSIONID", "DM_EXIT", "SE_COMPARATOR_SHOULD_BE_SERIALIZABLE"))
                .because("TODO", //TODO
                        In.everywhere().ignoreAll())
        ).analyze();
    }

    @Override
    protected PmdResult analyzePmd() {
        return new PmdAnalyzer(config, new PmdViolationCollector() //.minPriority(RulePriority.MEDIUM)
//                .because("I don't agree", In.everywhere().ignore(
//                        "AvoidFieldNameMatchingMethodName", "AvoidFieldNameMatchingTypeName", "CommentDefaultAccessModifier",
//                        "MethodArgumentCouldBeFinal", "UncommentedEmptyMethodBody", "VariableNamingConventions",
//                        "AvoidInstantiatingObjectsInLoops","AvoidSynchronizedAtMethodLevel"))
//                .because("equals is complex", In.locs("#equals", "#hashCode").ignore(
//                        "CyclomaticComplexity", "StdCyclomaticComplexity", "ModifiedCyclomaticComplexity", "NPathComplexity"))
                .because("TODO",  //TODO
                        In.everywhere().ignoreAll())
        ).withRulesets(basic(), braces(), codesize(),
                comments().requirement(Comments.Requirement.Ignored).maxLines(20),
                design(), empty(), exceptions(), imports(),
                junit(),
                naming().variableLen(1, 25),
                optimizations(), strings(), sunSecure(), typeResolution(), unnecessary(), unused())
                .analyze();
    }

    @Override
    protected CpdResult analyzeCpd() {
        return new CpdAnalyzer(config, 25, new CpdMatchCollector().just(
                In.classes(Command.class).ignore("public void execute(CommandContext context, PrintWriter out) throws IOException {"),
                In.loc("*Parser").ignoreAll(),
                In.everywhere().ignore("public boolean equals(Object o) {")
        )).analyze();
    }
}
