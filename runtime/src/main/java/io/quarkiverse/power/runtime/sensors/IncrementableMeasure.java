package io.quarkiverse.power.runtime.sensors;

import io.quarkiverse.power.runtime.SensorMeasure;

public interface IncrementableMeasure extends SensorMeasure {

    void addCPU(double v);

    default void addGPU(double v) {
    }
}
