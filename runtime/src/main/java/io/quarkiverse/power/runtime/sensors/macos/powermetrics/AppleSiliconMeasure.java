package io.quarkiverse.power.runtime.sensors.macos.powermetrics;

import java.util.Optional;

import io.quarkiverse.power.runtime.PowerSensor;

public class AppleSiliconMeasure implements PowerSensor.Measure {
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

    void addCPU(double v) {
        cpu += v;
    }

    void addGPU(double v) {
        gpu += v;
    }

    void addANE(double v) {
        ane += v;
    }

    @Override
    public int numberOfSamples() {
        return samplesNb;
    }

    void incrementSamples() {
        samplesNb++;
    }

    @Override
    public long measureDuration() {
        return System.currentTimeMillis() - startedAt;
    }
}
