package io.quarkiverse.power.runtime;

public interface PowerMeasure<M extends SensorMeasure> extends SensorMeasure {
    int numberOfSamples();

    long duration();

    M sensorMeasure();

    default double average() {
        return total() / numberOfSamples();
    }

    static String asString(PowerMeasure<?> measure) {
        final var durationInSeconds = measure.duration() / 1000;
        final var samples = measure.numberOfSamples();
        final var measuredMilliWatts = measure.total();
        return String.format("%s / avg: %s (%ds, %s samples)", readableWithUnit(measuredMilliWatts),
                readableWithUnit(measure.average()), durationInSeconds,
                samples);
    }

    static String readableWithUnit(double milliWatts) {
        String unit = milliWatts >= 1000 ? "W" : "mW";
        double power = milliWatts >= 1000 ? milliWatts / 1000 : milliWatts;
        return String.format("%.3f%s", power, unit);
    }
}