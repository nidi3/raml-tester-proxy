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

import guru.nidi.ramltester.core.RamlValidator;

/**
 *
 */
public interface ValidatorConfigurator {
    ValidatorConfigurator DEFAULT = new ValidatorConfigurator() {
        @Override
        public RamlValidator configure(RamlValidator validator) {
            return validator;
        }

        @Override
        public String asCli() {
            return "-v";
        }
    };
    ValidatorConfigurator NONE = new ValidatorConfigurator() {
        @Override
        public RamlValidator configure(RamlValidator validator) {
            return validator.withChecks();
        }

        @Override
        public String asCli() {
            return "";
        }
    };

    RamlValidator configure(RamlValidator validator);

    String asCli();

}
