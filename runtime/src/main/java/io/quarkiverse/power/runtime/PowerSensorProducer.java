package io.quarkiverse.power.runtime;

import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;

import io.quarkiverse.power.runtime.sensors.macos.powermetrics.MacOSPowermetricsSensor;

@Singleton
public class PowerSensorProducer {
    @Produces
    public PowerSensor sensor() {
        return new MacOSPowermetricsSensor();
    }
}
