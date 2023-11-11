package io.quarkiverse.power.runtime;

import java.util.List;

public interface PowerMeasure extends SensorMeasure {
    int numberOfSamples();

    long duration();

    default double average() {
        return total() / numberOfSamples();
    }

    default double standardDeviation() {
        final var mean = average();
        double sumOfSquares = measures().stream()
                .mapToDouble(measure -> Math.pow(measure[0] - mean, 2))
                .sum();
        return Math.sqrt(sumOfSquares / (numberOfSamples() - 1));
    }

    static String asString(PowerMeasure measure) {
        final var durationInSeconds = measure.duration() / 1000;
        final var samples = measure.numberOfSamples();
        final var measuredMilliWatts = measure.total();
        return String.format("%s / avg: %s / std dev: %.3f (%ds, %s samples)", readableWithUnit(measuredMilliWatts),
                readableWithUnit(measure.average()), measure.standardDeviation(), durationInSeconds,
                samples);
    }

    static String readableWithUnit(double milliWatts) {
        String unit = milliWatts >= 1000 ? "W" : "mW";
        double power = milliWatts >= 1000 ? milliWatts / 1000 : milliWatts;
        return String.format("%.3f%s", power, unit);
    }

    List<double[]> measures();
}