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

/**
 *
 */
public class LogNormalRandom {
    //TODO private
     final NormalRandom normalRandom;
    private final double factor;

    private LogNormalRandom(double mu, double sigma, double factor) {
        normalRandom = new NormalRandom(mu, sigma);
        this.factor = factor;
    }

    public static LogNormalRandom byMuSigma(double mu, double sigma) {
        return new LogNormalRandom(mu, sigma, 1);
    }

    public static LogNormalRandom byQunatiles(final double q50Value, double qLevel, double qValue) {
        if ((qValue > q50Value && qLevel < .5) || (qValue < q50Value && qLevel > .5)) {
            throw new IllegalArgumentException("Illegal quantiles");
        }
        final double q = qValue / q50Value;
        double approx = approxMuZeroByQuantile(qLevel, q, 10, 10);
        approx = approxMuZeroByQuantile(qLevel, q, approx, 1);
        approx = approxMuZeroByQuantile(qLevel, q, approx, .1);
        return new LogNormalRandom(0, approx, q50Value);
    }

    public double nextLogNormal() {
        return Math.exp(normalRandom.nextNormal()[0]) * factor;
    }

    public double density(double x) {
        final double s2 = 2 * normalRandom.sigma * normalRandom.sigma;
        final double f = 1 / Math.sqrt(Math.PI * s2);
        if (x <= 0) {
            return 0;
        }
        final double a = Math.log(x) - normalRandom.mu;
        return f / x * Math.exp(-a * a / s2);
    }

    //supposed: mu = 0 -> F(1) = .5
    private static double approxMuZeroByQuantile(double qLevel, double qValue, double sigma, double sigmaDelta) {
        double res = 0, error = 1000;
        for (double s = sigma - sigmaDelta; s < sigma + sigmaDelta; s += sigmaDelta / 10) {
            final LogNormalRandom rnd = LogNormalRandom.byMuSigma(0, s);
            final double d1 = rnd.distribution(1) - .5;
            final double d2 = rnd.distribution(qValue) - qLevel;
            final double e = d1 * d1 + d2 * d2;
            if (e < error) {
                res = s;
                error = e;
            }
        }
        return res;
    }

    private double distribution(double x) {
        double s = integrateDistribution(0, .000001, .00000000001) + integrateDistribution(.000001, .01, .000001) + integrateDistribution(.01, x, .01);
        return 1 / (Math.sqrt(2 * Math.PI) * normalRandom.sigma) * s;
    }

    private double integrateDistribution(double start, double end, double step) {
        double s = 0;
        for (double i = start + step / 2; i < end; i += step) {
            final double a = Math.log(i) - normalRandom.mu;
            s += step * 1 / i * Math.exp(-a * a / (2 * normalRandom.sigma * normalRandom.sigma));
        }
        return s;
    }
}
