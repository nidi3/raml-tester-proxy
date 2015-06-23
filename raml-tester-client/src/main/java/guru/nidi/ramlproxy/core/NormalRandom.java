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

import java.util.Random;

/**
 *
 */
public class NormalRandom {
    private final Random rnd = new Random();
    final double mu, sigma;

    public NormalRandom(double mu, double sigma) {
        this.mu = mu;
        this.sigma = sigma;
    }

    public double[] nextNormal() {
        double u1, u2, q;
        do {
            u1 = rnd.nextDouble() * 2 - 1;
            u2 = rnd.nextDouble() * 2 - 1;
            q = u1 * u1 + u2 * u2;
        } while (q == 0 || q >= 1);
        final double p = Math.sqrt(-2 * Math.log(q) / q);
        return new double[]{mu + sigma * u1 * p, mu + sigma * u2 * p};
    }

    public double[] nextNormal2() {
        final double u1 = rnd.nextDouble();
        final double u2 = rnd.nextDouble();
        return new double[]{mu + sigma * Math.cos(2 * Math.PI * u1) * Math.sqrt(-2 * Math.log(u2)), mu + sigma * Math.sin(2 * Math.PI * u2) * Math.sqrt(-2 * Math.log(u1))};
    }

    public double density(double x) {
        final double s2 = 2 * sigma * sigma;
        final double f = 1 / Math.sqrt(Math.PI * s2);
        final double a = x - mu;
        return f * Math.exp(-a * a / s2);
    }
}
