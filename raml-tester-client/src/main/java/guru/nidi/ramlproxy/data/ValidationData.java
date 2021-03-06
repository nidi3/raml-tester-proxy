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
package guru.nidi.ramlproxy.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import guru.nidi.ramltester.core.RamlViolations;

import java.util.List;

import static guru.nidi.ramlproxy.data.Converters.violationMessages;

public class ValidationData {
    private final String ramlTitle;
    private final List<String> validationViolations;

    public ValidationData(@JsonProperty("ramlTitle") String ramlTitle,
                          @JsonProperty("validationViolations") List<String> validationViolations) {
        this.ramlTitle = ramlTitle;
        this.validationViolations = validationViolations;
    }

    public static ValidationData of(String ramlTitle, RamlViolations violations) {
        return new ValidationData(ramlTitle, violationMessages(violations));
    }

    public String getRamlTitle() {
        return ramlTitle;
    }

    public List<String> getValidationViolations() {
        return validationViolations;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final ValidationData that = (ValidationData) o;

        if (ramlTitle != null ? !ramlTitle.equals(that.ramlTitle) : that.ramlTitle != null) {
            return false;
        }
        return !(validationViolations != null ? !validationViolations.equals(that.validationViolations) : that.validationViolations != null);

    }

    @Override
    public int hashCode() {
        int result = ramlTitle != null ? ramlTitle.hashCode() : 0;
        result = 31 * result + (validationViolations != null ? validationViolations.hashCode() : 0);
        return result;
    }
}
