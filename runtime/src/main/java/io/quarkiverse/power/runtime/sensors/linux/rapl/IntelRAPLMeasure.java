package io.quarkiverse.power.runtime.sensors.linux.rapl;

import java.util.Arrays;

import io.quarkiverse.power.runtime.SensorMeasure;
import io.quarkiverse.power.runtime.SensorMetadata;

public class IntelRAPLMeasure implements SensorMeasure {

    private final SensorMetadata metadata;
    private final double[] measure;

    IntelRAPLMeasure(SensorMetadata metadata, double[] measure) {
        if (measure.length != metadata.componentCardinality()) {
            throw new IllegalArgumentException(
                    "Provided measure " + Arrays.toString(measure) + " doesn't match provided metadata: " + metadata);
        }
        this.metadata = metadata;
        this.measure = measure;
    }

    @Override
    public double total() {
        double total = 0.0;
        for (double value : measure) {
            total += value;
        }
        return total;
    }

    @Override
    public SensorMetadata metadata() {
        return metadata;
    }
}
