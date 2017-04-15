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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.*;

public class CollectionUtils {

    public static Map map(Object... keysValues) {
        final Map<Object, Object> map = new HashMap<>();
        for (int i = 0; i < keysValues.length; i += 2) {
            map.put(keysValues[i], keysValues[i + 1]);
        }
        return map;
    }

    public static List list(Object... values) {
        return Arrays.asList(values);
    }

    public static void assertUsageMap(Map<String, List<String>> values, Map<String, List<String>> usage) {
        assertNotNull(usage);
        for (Map.Entry<String, List<String>> value : values.entrySet()) {
            assertThat(usage.get(value.getKey()), containsInAnyOrder(value.getValue().toArray()));
        }
    }

    public static void assertUsageMap(String name, Map<String, List<String>> values, Map<String, Object> usages) {
        assertEquals(1, usages.size());
        assertUsageMap(values, (Map<String, List<String>>) usages.get(name));
    }

}
