package io.quarkiverse.power.runtime.sensors.macos;

import java.util.Optional;

import io.quarkiverse.power.runtime.PowerSensor;

public class AppleSiliconMeasure implements PowerSensor.IncrementableMeasure {
    private double cpu;
    private double gpu;
    private double ane;
    private int samplesNb;
    private final long startedAt = System.currentTimeMillis();

    public static final String ANE = "ane";

    @Override
    public double cpu() {
        return cpu;
    }

    @Override
    public Optional<Double> gpu() {
        return Optional.of(gpu);
    }

    @Override
    public Optional<Double> byKey(String key) {
        return switch (key) {
            case CPU -> Optional.of(cpu());
            case GPU -> gpu();
            case ANE -> Optional.of(ane);
            case TOTAL -> Optional.of(total());
            default -> Optional.empty();
        };
    }

    @Override
    public double total() {
        return cpu + gpu + ane;
    }

    public void addCPU(double v) {
        cpu += v;
    }

    public void addGPU(double v) {
        gpu += v;
    }

    public void addANE(double v) {
        ane += v;
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
}
