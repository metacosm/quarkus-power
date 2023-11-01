package io.quarkiverse.power.runtime.sensors;

public interface PowerSensor<T extends IncrementableMeasure> {

    OngoingPowerMeasure<T> start(long duration, long frequency, Writer out) throws Exception;

    default void stop() {
    }

    void update(OngoingPowerMeasure<T> ongoingMeasure, Writer out);

    default void additionalInfo(Writer out) {
    }

    interface Writer {
        void println(String message);
    }
}
