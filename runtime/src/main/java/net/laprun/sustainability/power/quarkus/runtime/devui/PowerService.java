package net.laprun.sustainability.power.quarkus.runtime.devui;

import jakarta.inject.Inject;

import net.laprun.sustainability.power.quarkus.runtime.PowerMeasurer;

public class PowerService {
    @Inject
    PowerMeasurer measurer;

    public String info() {
        return measurer.sampler().info();
    }
}
