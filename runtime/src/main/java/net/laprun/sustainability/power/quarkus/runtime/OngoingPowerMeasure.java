package net.laprun.sustainability.power.quarkus.runtime;

import java.util.ArrayList;
import java.util.Arrays;

import net.laprun.sustainability.power.SensorMetadata;

public class OngoingPowerMeasure extends AbstractPowerMeasure {
    private final long startedAt;
    private double minTotal = Double.MAX_VALUE;
    private double maxTotal;
    private final double[] totals;

    public OngoingPowerMeasure(SensorMetadata sensorMetadata, long duration, long frequency) {
        super(sensorMetadata, new ArrayList<>((int) (duration / frequency)));
        startedAt = System.currentTimeMillis();
        final var numComponents = metadata().componentCardinality();
        totals = new double[numComponents];
    }

    public void recordMeasure(double[] components) {
        final var recorded = new double[components.length];
        System.arraycopy(components, 0, recorded, 0, components.length);
        measures().add(recorded);

        for (int i = 0; i < components.length; i++) {
            totals[i] += components[i];
        }

        // record min / max totals
        final var recordedTotal = sumOfComponents(recorded);
        if (recordedTotal < minTotal) {
            minTotal = recordedTotal;
        }
        if (recordedTotal > maxTotal) {
            maxTotal = recordedTotal;
        }
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
