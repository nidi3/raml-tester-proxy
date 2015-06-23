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

import org.apache.commons.math3.distribution.LogNormalDistribution;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 *
 */
public class LogNormalRandomTest {
    @Test
    public void wrongQuantiles() {
//        LogNormalRandom
    }

    @Test
    public void quantiles() {
        final LogNormalRandom rnd = LogNormalRandom.byQunatiles(.2, .8, 1);
        final int[] c = run(rnd, 10);
    }

    @Test
    public void testWithDistributionFunc() {
        for (int mu = 0; mu <= 4; mu++) {
            for (double sigma = .3; sigma < 2; sigma += .3) {
                final LogNormalRandom rnd = LogNormalRandom.byMuSigma(mu, sigma);
                checkWithDensityFunc(run(rnd, 150), rnd, 150);
            }
        }
    }

    private int[] run(LogNormalRandom rnd, double factor) {
        final LogNormalDistribution lnd = new LogNormalDistribution(rnd.normalRandom.mu, rnd.normalRandom.sigma);
        int[] c = new int[2000];
        for (int i = 0; i < 100000; i++) {
//            final int x = (int) Math.round(lnd.sample() * factor);
            final int x = (int) Math.ceil(rnd.nextLogNormal()*factor);
            if (x >= 0 && x < 2000) {
                c[x]++;
            }
        }
        return c;
    }

    private void checkWithDensityFunc(int[] c, LogNormalRandom rnd, double factor) {
        final LogNormalDistribution lnd = new LogNormalDistribution(rnd.normalRandom.mu, rnd.normalRandom.sigma);
        final double s = sum(c);
        for (int i = 1; i < c.length; i++) {
            System.out.printf("%2.3f %2.3f %2.3f %n", rnd.density(i / factor), factor * c[i] / s, lnd.density(i / factor));

            assertEquals(rnd.density(i / factor), factor * c[i] / s, .1);
        }
        System.out.println();
    }

    private double sum(int[] cs) {
        int s = 0;
        for (int c : cs) {
            s += c;
        }
        return s;
    }
}