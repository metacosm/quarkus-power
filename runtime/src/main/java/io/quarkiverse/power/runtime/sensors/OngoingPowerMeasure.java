package io.quarkiverse.power.runtime.sensors;

import java.util.Optional;

import io.quarkiverse.power.runtime.PowerMeasure;

public class OngoingPowerMeasure<M extends IncrementableMeasure>
        implements IncrementableMeasure, PowerMeasure<M> {
    private final M measure;
    private final long startedAt;
    private int samplesNb;

    public OngoingPowerMeasure(M measure) {
        startedAt = System.currentTimeMillis();
        samplesNb = 0;
        this.measure = measure;
    }

    public void incrementSamples() {
        samplesNb++;
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

    public int numberOfSamples() {
        return samplesNb;
    }

    public long duration() {
        return System.currentTimeMillis() - startedAt;
    }

    @Override
    public void addCPU(double v) {
        measure.addCPU(v);
    }

    @Override
    public void addGPU(double v) {
        measure.addGPU(v);
    }

    public M sensorMeasure() {
        return measure;
    }
}
