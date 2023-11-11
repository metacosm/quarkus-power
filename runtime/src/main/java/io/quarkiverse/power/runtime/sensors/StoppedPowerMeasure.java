package io.quarkiverse.power.runtime.sensors;

import java.util.List;

import io.quarkiverse.power.runtime.PowerMeasure;
import io.quarkiverse.power.runtime.SensorMetadata;

public class StoppedPowerMeasure implements PowerMeasure {
    private final SensorMetadata metadata;
    private final long duration;
    private final int samples;
    private final List<double[]> measures;
    private final double total;

    public StoppedPowerMeasure(PowerMeasure powerMeasure) {
        this.metadata = powerMeasure.metadata();
        this.duration = powerMeasure.duration();
        this.samples = powerMeasure.numberOfSamples();
        this.measures = powerMeasure.measures();
        this.total = powerMeasure.total();
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
    public List<double[]> measures() {
        return measures;
    }

    @Override
    public double total() {
        return total;
    }

    @Override
    public SensorMetadata metadata() {
        return metadata;
    }
}
