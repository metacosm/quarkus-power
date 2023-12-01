package io.quarkiverse.power.runtime.sensors;

import java.util.ArrayList;
import java.util.Arrays;

import io.quarkiverse.power.runtime.SensorMetadata;

public class OngoingPowerMeasure extends AbstractPowerMeasure {
    private final long startedAt;
    private double minTotal = Double.MAX_VALUE;
    private double maxTotal;
    private final double[] totals;
    private double[] current;

    public OngoingPowerMeasure(SensorMetadata sensorMetadata, long duration, long frequency) {
        super(sensorMetadata, new ArrayList<>((int) (duration / frequency)));
        startedAt = System.currentTimeMillis();
        final var numComponents = metadata().componentCardinality();
        totals = new double[numComponents];
    }

    public void startNewMeasure() {
        if (current != null) {
            throw new IllegalStateException("A new measure cannot be started while one is still ongoing");
        }
        current = new double[metadata().componentCardinality()];
    }

    public void setComponent(int index, double value) {
        if (current == null) {
            throw new IllegalStateException("A new measure must be started before recording components");
        }
        current[index] = value;
        totals[index] += value;
    }

    public void setComponents(double[] components) {
        current = components;
        for (int i = 0; i < components.length; i++) {
            totals[i] += components[i];
        }
    }

    public double[] stopMeasure() {
        if (current == null) {
            throw new IllegalStateException("Measure was not started so cannot be stopped");
        }
        final var recorded = new double[current.length];
        System.arraycopy(current, 0, recorded, 0, current.length);
        measures().add(recorded);

        // record min / max totals
        final var recordedTotal = sumOfComponents(recorded);
        if (recordedTotal < minTotal) {
            minTotal = recordedTotal;
        }
        if (recordedTotal > maxTotal) {
            maxTotal = recordedTotal;
        }

        current = null;
        return recorded;
    }

    @Override
    public double total() {
        return sumOfComponents(totals);
    }

    public long duration() {
        return System.currentTimeMillis() - startedAt;
    }

    @Override
    public double minMeasuredTotal() {
        return minTotal;
    }

    @Override
    public double maxMeasuredTotal() {
        return maxTotal;
    }

    @Override
    public double[] averagesPerComponent() {
        return Arrays.stream(totals).map(total -> total / numberOfSamples()).toArray();
    }
}
