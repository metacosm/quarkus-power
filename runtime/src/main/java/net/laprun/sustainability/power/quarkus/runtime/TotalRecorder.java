package net.laprun.sustainability.power.quarkus.runtime;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import net.laprun.sustainability.power.SensorMetadata;
import net.laprun.sustainability.power.SensorUnit;
import net.laprun.sustainability.power.analysis.Recorder;
import net.laprun.sustainability.power.analysis.total.TotalSyntheticComponent;

public class TotalRecorder extends TotalSyntheticComponent implements Recorder {
    private DescriptiveStatistics statistics = new DescriptiveStatistics();

    public TotalRecorder(SensorMetadata metadata, SensorUnit expectedResultUnit, int... totalComponentIndices) {
        super(metadata, expectedResultUnit, totalComponentIndices);
    }

    @Override
    public double[] liveMeasures() {
        return statistics.getValues();
    }

    @Override
    public double synthesizeFrom(double[] components, long timestamp) {
        final var value = super.synthesizeFrom(components, timestamp);
        statistics.addValue(value);
        return value;
    }

    public DescriptiveStatistics statistics() {
        return statistics;
    }

    public void reset() {
        statistics = new DescriptiveStatistics();
    }
}
