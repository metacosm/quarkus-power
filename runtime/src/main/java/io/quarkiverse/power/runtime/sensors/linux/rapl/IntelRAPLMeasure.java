package io.quarkiverse.power.runtime.sensors.linux.rapl;

import io.quarkiverse.power.runtime.sensors.IncrementableMeasure;

public class IntelRAPLMeasure implements IncrementableMeasure {
    private final double initial;
    private double cpu;
    private final long startedAt = System.currentTimeMillis();

    public IntelRAPLMeasure(double initial) {
        this.initial = initial;
    }

    @Override
    public double cpu() {
        return (cpu / durationSinceStart()) / 1_000;
    }

    @Override
    public void addCPU(double v) {
        cpu += ((long) v - initial);
    }

    private long durationSinceStart() {
        return System.currentTimeMillis() - startedAt;
    }
}
