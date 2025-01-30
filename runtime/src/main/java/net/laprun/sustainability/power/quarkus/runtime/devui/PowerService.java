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

    public boolean isRunning() {
        return measurer.isRunning();
    }

    public String status() {
        return measurer.sampler().status();
    }

    public List<ComponentMetadata> remoteMetadata() {
        return measurer.measureMetadata(converter).remote();
    }

    public List<ComponentMetadata> localMetadata() {
        return measurer.measureMetadata(converter).local();
    }

    public List<DisplayMeasure> measures() {
        return measures.measures().entrySet().stream()
                .map((e) -> new DisplayMeasure(e.getKey(), e.getValue()))
                .sorted()
                .toList();
    }

    public record DisplayMeasure(String name, List<Measures.Measure> measures) implements Comparable<DisplayMeasure> {
        @Override
        public int compareTo(DisplayMeasure o) {
            return name.compareTo(o.name);
        }
    }

    public record ComponentMetadata(String name, int index, String description, String unit) {}

    public DisplayableMeasure startOrStop(boolean start) {
        if(start) {
            measurer.start(0, 500);
            return null;
        } else {
            return measurer.stop().orElse(null);
        }
    }
}
