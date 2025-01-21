package net.laprun.sustainability.power.quarkus.runtime.devui;

import java.util.List;
import java.util.function.Function;

import jakarta.inject.Inject;

import net.laprun.sustainability.power.SensorMetadata;
import net.laprun.sustainability.power.quarkus.runtime.PowerMeasurer;
import net.laprun.sustainability.power.quarkus.runtime.ServerSampler;

public class PowerService {
    public static final Function<SensorMetadata.ComponentMetadata, ComponentMetadata> converter = cm -> new ComponentMetadata(cm.name(), cm.index(), cm.description(), cm.unitAsSymbol());
    @Inject
    PowerMeasurer measurer;

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

    public record ComponentMetadata(String name, int index, String description, String unit) {}

    public Result startOrStop(boolean start) {
        if(start) {
            measurer.start(0, 500);
            return null;
        } else {
            return measurer.stop().map(Result::new).orElse(null);
        }
    }

    public static class Result extends ServerSampler.TotalStoppedPowerMeasure {

        public Result(ServerSampler.TotalStoppedPowerMeasure stoppedMeasure) {
            super(stoppedMeasure);
        }

        public String getResult() {
            return toString();
        }

        public int getSamplesCount() {
            return numberOfSamples();
        }

        public double[] getMeasures() {
            return underlyingMeasure().getMeasuresFor(4).toArray();
        }
    }
}
