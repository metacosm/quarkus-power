package io.quarkiverse.power.runtime.sensors.linux.rapl;

import io.quarkiverse.power.runtime.sensors.IncrementableMeasure;

public class IntelRAPLMeasure implements IncrementableMeasure {
    private final long initial;
    private long cpu;
    private final long startedAt = System.currentTimeMillis();

    public IntelRAPLMeasure(long initial) {
        this.initial = initial;
    }

    @Override
    public double cpu() {
        return ((double) cpu / durationSinceStart()) / 1_000;
    }

    @Override
    public void addCPU(double v) {
        cpu += ((long) v - initial);
    }

    private long durationSinceStart() {
        return System.currentTimeMillis() - startedAt;
    }
}
