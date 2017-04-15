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
package guru.nidi.ramlproxy.cli;

import org.apache.commons.cli.Option;

import java.util.Comparator;

class OptionComparator implements Comparator<Option> {
    private final String options;

    public OptionComparator(String options) {
        this.options = options;
    }

    @Override
    public int compare(Option o1, Option o2) {
        return options.indexOf(o1.getOpt().charAt(0)) - options.indexOf(o2.getOpt().charAt(0));
    }
}
