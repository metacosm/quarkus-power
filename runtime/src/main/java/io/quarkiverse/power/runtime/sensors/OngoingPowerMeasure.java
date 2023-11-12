package io.quarkiverse.power.runtime.sensors;

import java.util.ArrayList;
import java.util.List;

import io.quarkiverse.power.runtime.PowerMeasure;
import io.quarkiverse.power.runtime.SensorMetadata;

public class OngoingPowerMeasure implements PowerMeasure {
    private final SensorMetadata sensorMetadata;
    private final long startedAt;
    private final List<double[]> measures = new ArrayList<>();
    private double[] current;
    private double total;

    public OngoingPowerMeasure(SensorMetadata sensorMetadata) {
        startedAt = System.currentTimeMillis();
        this.sensorMetadata = sensorMetadata;
    }

    public void startNewMeasure() {
        if (current != null) {
            throw new IllegalStateException("A new measure cannot be started while one is still ongoing");
        }
        current = new double[sensorMetadata.componentCardinality()];
    }

    public void setComponent(int index, double value) {
        if (current == null) {
            throw new IllegalStateException("A new measure must be started before recording components");
        }
        current[index] = value;
    }

    public double[] stopMeasure() {
        if (current == null) {
            throw new IllegalStateException("Measure was not started so cannot be stopped");
        }
        final var recorded = new double[current.length];
        System.arraycopy(current, 0, recorded, 0, current.length);
        measures.add(recorded);
        var currentMeasureTotal = 0.0;
        for (double value : recorded) {
            currentMeasureTotal += value;
        }
        total += currentMeasureTotal;
        current = null;
        return recorded;
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
        return sensorMetadata;
    }

    public int numberOfSamples() {
        return measures.size();
    }

    public long duration() {
        return System.currentTimeMillis() - startedAt;
    }
}
