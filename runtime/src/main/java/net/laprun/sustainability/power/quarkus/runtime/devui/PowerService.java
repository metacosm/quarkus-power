package net.laprun.sustainability.power.quarkus.runtime.devui;

import java.util.List;
import java.util.function.Function;

import jakarta.inject.Inject;

import net.laprun.sustainability.power.SensorMetadata;
import net.laprun.sustainability.power.quarkus.runtime.DisplayableMeasure;
import net.laprun.sustainability.power.quarkus.runtime.Measures;
import net.laprun.sustainability.power.quarkus.runtime.PowerMeasurer;

@SuppressWarnings("unused")
public class PowerService {
    public static final Function<SensorMetadata.ComponentMetadata, ComponentMetadata> converter = cm -> new ComponentMetadata(cm.name(), cm.index(), cm.description(), cm.unitAsSymbol());
    @Inject
    PowerMeasurer measurer;

    @Inject
    Measures measures;

    private DisplayableMeasure measure;

    public boolean isRunning() {
        return measurer.isRunning();
    }

    public String status() {
        return measurer.sampler().status();
    }

    public List<ComponentMetadata> metadata() {
        return measurer.measureMetadata(converter).components();
    }

    public List<DisplayMeasure> measures() {
        return measures.measures().entrySet().stream()
                .map(e -> new DisplayMeasure(e.getKey(), e.getValue().stream().map(this::from).toList()))
                .sorted()
                .toList();
    }

    public record DisplayMeasure(String name, List<MethodMeasure> measures) implements Comparable<DisplayMeasure> {
        @Override
        public int compareTo(DisplayMeasure o) {
            return name.compareTo(o.name);
        }
    }

    public record MethodMeasure(long durationMs, double power)  {
    }

    public MethodMeasure from(Measures.Measure measure) {
        final var cursor = measurer.sampler().cursorOver(measure.startTime(), measure.duration());
        return new MethodMeasure(measure.duration().toMillis(), cursor.sum());
    }

    public record ComponentMetadata(String name, int index, String description, String unit) {}

    public DisplayableMeasure startOrStop(boolean start) {
        if(start) {
            measurer.start(0, 500);
            return null;
        } else {
            measure = measurer.stop().orElse(null);
            return measure;
        }
    }
}
