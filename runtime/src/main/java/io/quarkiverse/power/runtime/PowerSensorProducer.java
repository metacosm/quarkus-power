package io.quarkiverse.power.runtime;

import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;

@Singleton
public class PowerSensorProducer {
    @Produces
    public PowerSensor sensor() {
        return new MacOSPowermetricsSensor();
    }
}
