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

import guru.nidi.ramltester.core.RamlValidator;
import guru.nidi.ramltester.core.Validation;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ValidatorConfigurator {
    public static final ValidatorConfigurator
            DEFAULT = new ValidatorConfigurator("-v", Arrays.asList(Validation.values()), null, null, null),
            NONE = new ValidatorConfigurator("", Collections.<Validation>emptyList(), null, null, null);

    private final String cli;
    private final List<Validation> validations;
    private final String resourcePattern;
    private final String parameterPattern;
    private final String headerPattern;

    public ValidatorConfigurator(String cli, List<Validation> validations, String resourcePattern, String parameterPattern, String headerPattern) {
        this.cli = cli;
        this.validations = validations;
        this.resourcePattern = resourcePattern;
        this.parameterPattern = parameterPattern;
        this.headerPattern = headerPattern;
    }


    public RamlValidator configure(RamlValidator validator) {
        return validator
                .withChecks(validations.toArray(new Validation[validations.size()]))
                .withResourcePattern(resourcePattern)
                .withParameterPattern(parameterPattern)
                .withHeaderPattern(headerPattern);
    }

    public String asCli() {
        return cli;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final ValidatorConfigurator that = (ValidatorConfigurator) o;

        if (cli != null ? !cli.equals(that.cli) : that.cli != null) {
            return false;
        }
        if (validations != null ? !validations.equals(that.validations) : that.validations != null) {
            return false;
        }
        if (resourcePattern != null ? !resourcePattern.equals(that.resourcePattern) : that.resourcePattern != null) {
            return false;
        }
        if (parameterPattern != null ? !parameterPattern.equals(that.parameterPattern) : that.parameterPattern != null) {
            return false;
        }
        return !(headerPattern != null ? !headerPattern.equals(that.headerPattern) : that.headerPattern != null);

    }

    @Override
    public int hashCode() {
        int result = cli != null ? cli.hashCode() : 0;
        result = 31 * result + (validations != null ? validations.hashCode() : 0);
        result = 31 * result + (resourcePattern != null ? resourcePattern.hashCode() : 0);
        result = 31 * result + (parameterPattern != null ? parameterPattern.hashCode() : 0);
        result = 31 * result + (headerPattern != null ? headerPattern.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ValidatorConfigurator{" +
                "validations=" + validations +
                ", resourcePattern='" + resourcePattern + '\'' +
                ", parameterPattern='" + parameterPattern + '\'' +
                ", headerPattern='" + headerPattern + '\'' +
                '}';
    }
}
