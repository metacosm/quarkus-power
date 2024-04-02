package net.laprun.sustainability.power.quarkus.runtime;

import net.laprun.sustainability.power.SensorMetadata;

import java.util.List;
import java.util.stream.IntStream;

public interface PowerMeasure {
    int numberOfSamples();

    long duration();

    default double average() {
        return total() / numberOfSamples();
    }

    double total();

    SensorMetadata metadata();

    double[] averagesPerComponent();

    double minMeasuredTotal();

    double maxMeasuredTotal();

    default double standardDeviation() {
        final var cardinality = metadata().componentCardinality();
        final var samples = numberOfSamples() - 1; // unbiased so we remove one sample
        // need to compute the average of variances then square root that to get the "aggregate" standard deviation, see: https://stats.stackexchange.com/a/26647
        // "vectorize" computation of variances: compute the variance for each component in parallel
        final var componentVarianceAverage = IntStream.range(0, cardinality).parallel()
                // compute variances for each component of the measure
                .mapToDouble(component -> {
                    final var squaredDifferenceSum = measures().stream().parallel()
                            .mapToDouble(m -> Math.pow(m[component] - averagesPerComponent()[component], 2))
                            .sum();
                    return squaredDifferenceSum / samples;
                })
                .average()
                .orElse(0.0);
        return Math.sqrt(componentVarianceAverage);
    }

    static String asString(PowerMeasure measure) {
        final var durationInSeconds = measure.duration() / 1000;
        final var samples = measure.numberOfSamples();
        final var measuredMilliWatts = measure.total();
        return String.format("%s / avg: %s / std dev: %.3f [min: %.3f, max: %.3f] (%ds, %s samples)",
                readableWithUnit(measuredMilliWatts),
                readableWithUnit(measure.average()), measure.standardDeviation(), measure.minMeasuredTotal(),
                measure.maxMeasuredTotal(), durationInSeconds,
                samples);
    }

    static String readableWithUnit(double milliWatts) {
        String unit = milliWatts >= 1000 ? "W" : "mW";
        double power = milliWatts >= 1000 ? milliWatts / 1000 : milliWatts;
        return String.format("%.3f%s", power, unit);
    }

    List<double[]> measures();
}
