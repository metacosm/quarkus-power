package io.quarkiverse.power.runtime.sensors;

import io.quarkiverse.power.runtime.PowerMeasure;

public interface PowerSensor<T extends IncrementableMeasure> {

    OngoingPowerMeasure<T> start(long duration, long frequency, Writer out) throws Exception;

    default void stop() {
    }

    void update(OngoingPowerMeasure<T> ongoingMeasure, Writer out);

    default void additionalInfo(PowerMeasure<T> measure, Writer out) {
    }

    interface Writer {
        void println(String message);
    }
}
