package io.quarkiverse.power.runtime;

import java.util.List;

abstract class AbstractPowerMeasure implements PowerMeasure {
    private final SensorMetadata sensorMetadata;
    private final List<double[]> measures;

    protected AbstractPowerMeasure(SensorMetadata sensorMetadata, List<double[]> measures) {
        this.sensorMetadata = sensorMetadata;
        this.measures = measures;
    }

    public static double sumOfComponents(double[] recorded) {
        var componentSum = 0.0;
        for (double value : recorded) {
            componentSum += value;
        }
        return componentSum;
    }

    @Override
    public List<double[]> measures() {
        return measures;
    }

    @Override
    public SensorMetadata metadata() {
        return sensorMetadata;
    }

    public int numberOfSamples() {
        return measures.size();
    }
}
