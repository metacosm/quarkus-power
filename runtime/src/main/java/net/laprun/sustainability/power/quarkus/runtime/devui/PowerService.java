package net.laprun.sustainability.power.quarkus.runtime.devui;

import jakarta.inject.Inject;

import net.laprun.sustainability.power.quarkus.runtime.PowerMeasurer;

public class PowerService {
    @Inject
    PowerMeasurer measurer;

    public String info() {
        return measurer.measureMetadata().status();
    }

    public String remoteMetadata() {
        return measurer.measureMetadata().remote()
                .map(Object::toString)
                .orElse("Could not get remote metadata");
    }

    public String localMetadata() {
        return measurer.measureMetadata().local()
                .map(Object::toString)
                .orElse("No ongoing measure");
    }
}
