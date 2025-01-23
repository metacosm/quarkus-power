package net.laprun.sustainability.power.quarkus.runtime.devui;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import jakarta.inject.Inject;

import net.laprun.sustainability.power.SensorMetadata;
import net.laprun.sustainability.power.quarkus.runtime.DisplayableMeasure;
import net.laprun.sustainability.power.quarkus.runtime.Measures;
import net.laprun.sustainability.power.quarkus.runtime.PowerMeasurer;

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

    public Map<String, DisplayableMeasure> measures() {
        return measures.measures();
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
