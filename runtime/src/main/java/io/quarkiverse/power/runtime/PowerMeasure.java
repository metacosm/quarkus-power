package io.quarkiverse.power.runtime;

public interface PowerMeasure<M extends SensorMeasure> extends SensorMeasure {
    int numberOfSamples();

    long duration();

    M sensorMeasure();
}
