package io.quarkiverse.power.runtime.sensors;

import java.util.Optional;

import io.quarkiverse.power.runtime.PowerMeasure;

public interface PowerSensor<T extends IncrementableMeasure> {

    OngoingPowerMeasure<T> start(long duration, long frequency) throws Exception;

    default void stop() {
    }

    void update(OngoingPowerMeasure<T> ongoingMeasure);

    default Optional<String> additionalInfo(PowerMeasure<T> measure) {
        return Optional.empty();
    }
}
