package io.quarkiverse.power.runtime.sensors;

import java.util.Optional;

import io.quarkiverse.power.runtime.PowerMeasure;
import io.quarkiverse.power.runtime.SensorMeasure;

public interface PowerSensor<T extends SensorMeasure> {

    OngoingPowerMeasure start(long duration, long frequency) throws Exception;

    default void stop() {
    }

    void update(OngoingPowerMeasure ongoingMeasure);

    default Optional<String> additionalInfo(PowerMeasure measure) {
        return Optional.empty();
    }

    T measureFor(double[] measureComponents);
}
