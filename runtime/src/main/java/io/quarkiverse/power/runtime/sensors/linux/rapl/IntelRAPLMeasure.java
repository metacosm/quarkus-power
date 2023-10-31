package io.quarkiverse.power.runtime.sensors.linux.rapl;

import java.util.Optional;

import io.quarkiverse.power.runtime.PowerSensor;

public class IntelRAPLMeasure implements PowerSensor.IncrementableMeasure {
    private final long initial;
    private final long startedAt = System.currentTimeMillis();
    private int samplesNb;

    private long cpu;

    public IntelRAPLMeasure(long initial) {
        this.initial = initial;
    }

    @Override
    public double cpu() {
        return ((double) cpu / measureDuration()) / 1_000;
    }

    @Override
    public Optional<Double> gpu() {
        return Optional.empty();
    }

    @Override
    public Optional<Double> byKey(String key) {
        return Optional.empty();
    }

    @Override
    public double total() {
        return cpu();
    }

    @Override
    public int numberOfSamples() {
        return samplesNb;
    }

    public void incrementSamples() {
        samplesNb++;
    }

    @Override
    public long measureDuration() {
        return System.currentTimeMillis() - startedAt;
    }

    @Override
    public void addCPU(double v) {
        cpu += ((long) v - initial);
        System.out.println("cpu = " + cpu);
    }

    @Override
    public void addGPU(double v) {

    }
}
