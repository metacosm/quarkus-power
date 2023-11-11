package io.quarkiverse.power.runtime.sensors.macos;

import io.quarkiverse.power.runtime.SensorMeasure;
import io.quarkiverse.power.runtime.SensorMetadata;

public class AppleSiliconMeasure implements SensorMeasure {
    private final double cpu;
    private final double gpu;
    private final double ane;
    public static final String ANE = "ane";
    public static final String CPU = "cpu";
    public static final String GPU = "gpu";
    public static final SensorMetadata METADATA = new SensorMetadata() {
        @Override
        public int indexFor(String component) {
            return switch (component) {
                case CPU -> 0;
                case GPU -> 1;
                case ANE -> 2;
                default -> throw new IllegalArgumentException("Unknown component: " + component);
            };
        }

        @Override
        public int componentCardinality() {
            return 3;
        }
    };

    public AppleSiliconMeasure(double[] components) {
        this.cpu = components[METADATA.indexFor(CPU)];
        this.gpu = components[METADATA.indexFor(GPU)];
        this.ane = components[METADATA.indexFor(ANE)];
    }

    @Override
    public double total() {
        return cpu + gpu + ane;
    }

    @Override
    public SensorMetadata metadata() {
        return METADATA;
    }

    public double cpu() {
        return cpu;
    }
}
