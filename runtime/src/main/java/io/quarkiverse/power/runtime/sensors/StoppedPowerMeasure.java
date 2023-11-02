package io.quarkiverse.power.runtime.sensors;

import java.util.Optional;

import io.quarkiverse.power.runtime.PowerMeasure;
import io.quarkiverse.power.runtime.SensorMeasure;

public class StoppedPowerMeasure<M extends SensorMeasure> implements SensorMeasure, PowerMeasure<M> {
    private final M measure;
    private final long duration;
    private final int samples;

    public StoppedPowerMeasure(PowerMeasure<M> powerMeasure) {
        this.measure = powerMeasure.sensorMeasure();
        this.duration = powerMeasure.duration();
        this.samples = powerMeasure.numberOfSamples();
    }

    @Override
    public double cpu() {
        return measure.cpu();
    }

    @Override
    public Optional<Double> gpu() {
        return measure.gpu();
    }

    @Override
    public Optional<Double> byKey(String key) {
        return measure.byKey(key);
    }

    @Override
    public double total() {
        return measure.total();
    }

    @Override
    public int numberOfSamples() {
        return samples;
    }

    @Override
    public long duration() {
        return duration;
    }

    @Override
    public M sensorMeasure() {
        return measure;
    }
}
