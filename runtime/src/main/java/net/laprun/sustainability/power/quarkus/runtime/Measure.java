package net.laprun.sustainability.power.quarkus.runtime;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import net.laprun.sustainability.power.SensorMetadata;
import net.laprun.sustainability.power.SensorUnit;
import net.laprun.sustainability.power.measure.MeasureBackedCursor;
import net.laprun.sustainability.power.measure.OngoingPowerMeasure;

class Measure {
    private OngoingPowerMeasure measure;
    private DisplayableMeasure measured;
    private TotalRecorder totalComp;
    private boolean running;

    List<SensorMetadata.ComponentMetadata> localMetadata() {
        // todo: generify
        return Optional.ofNullable(measure)
                .map(m -> List.of(m.metadata().metadataFor(4)))
                .orElse(List.of());
    }

    boolean isRunning() {
        return running;
    }

    void startWith(SensorMetadata metadata) {
        if (totalComp == null) {
            // default total aggregation: all known components that W (or similar) as unit
            final var integerIndices = metadata.components().values().stream()
                    .filter(cm -> SensorUnit.W.isCommensurableWith(cm.unit()))
                    .map(SensorMetadata.ComponentMetadata::index)
                    .toArray(Integer[]::new);
            if (integerIndices.length > 0) {
                final int[] totalIndices = new int[integerIndices.length];
                for (int i = 0; i < totalIndices.length; i++) {
                    totalIndices[i] = integerIndices[i];
                }
                totalComp = new TotalRecorder(metadata, SensorUnit.mW, totalIndices);
            }
        } else {
            totalComp.reset();
        }

        if(measure == null) {
            // fixme: remove hardcoded sample period
            measure = new OngoingPowerMeasure(metadata, 500, totalComp);
        } else {
            measure.reset();
        }

        measured = null;
        running = true;
    }

    void recordMeasure(double[] components) {
        measure.recordMeasure(components);
    }

    DisplayableMeasure stop() {
        running = false;
        final var stats = totalComp.statistics();
        final var timing = measure.timingInfo();
        measured = new DisplayableMeasure(stats.getSum(), stats.getMin(), stats.getMax(), stats.getMean(), stats.getStandardDeviation(), stats.getValues(), timing);
        return measured;
    }

    MeasureBackedCursor cursorOver(long timestamp, Duration duration) {
        final var timing = measured != null ? measured.timestamps() : measure.timingInfo();
        return new MeasureBackedCursor(totalComp, timing.cursorOver(timestamp, duration));
    }
}
