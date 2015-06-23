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

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 *
 */
public class NormalRandomTest {
    private static final double[] DENSITY_FOR_10_25 = new double[]{
            5.35E-5, 2.45E-4, 9.54E-4,
            0.003, 0.009, 0.022,
            0.044, 0.078, 0.116,
            0.147, 0.160, 0.147,
            0.116, 0.078, 0.044,
            0.022, 0.009, 0.003,
            9.54E-4, 2.45E-4};

    @Test
    public void test10_25() {
        final NormalRandom rnd = new NormalRandom(10, 2.5);
        final int[] c = run(rnd);
        final double s = sum(c);
        for (int i = 0; i < c.length; i++) {
            assertEquals(DENSITY_FOR_10_25[i], c[i] / s, .01);
        }
    }

    @Test
    public void testWithDistributionFunc() {
        for (int mu = 5; mu <= 15; mu += 5) {
            for (double sigma = 1; sigma < 4; sigma += .5) {
                final NormalRandom rnd = new NormalRandom(mu, sigma);
                checkWithDensityFunc(run(rnd), rnd);
            }
        }
    }

    private int[] run(NormalRandom rnd) {
        int[] c = new int[20];
        for (int i = 0; i < 100000; i++) {
            final double[] n = rnd.nextNormal2();
            for (int j = 0; j < 2; j++) {
                final int x = (int) Math.round(n[j]);
                if (x >= 0 && x < 20) {
                    c[x]++;
                }
            }
        }
        return c;
    }

    private void checkWithDensityFunc(int[] c, NormalRandom rnd) {
        final double s = sum(c);
        for (int i = 0; i < c.length; i++) {
//            System.out.printf("%2.3f %2.3f %n", rnd.density(i), c[i] / s);
            assertEquals(rnd.density(i), c[i] / s, .02);
        }
    }

    private double sum(int[] cs) {
        int s = 0;
        for (int c : cs) {
            s += c;
        }
        return s;
    }
}