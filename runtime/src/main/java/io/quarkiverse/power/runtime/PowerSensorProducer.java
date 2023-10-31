package io.quarkiverse.power.runtime;

import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;

import io.quarkiverse.power.runtime.sensors.linux.rapl.IntelRAPLSensor;
import io.quarkiverse.power.runtime.sensors.macos.powermetrics.MacOSPowermetricsSensor;

@Singleton
public class PowerSensorProducer {
    @Produces
    public PowerSensor<?> sensor() {
        final var originalOSName = System.getProperty("os.name");
        String osName = originalOSName.toLowerCase();

        if (osName.contains("mac os x")) {
            return MacOSPowermetricsSensor.instance;
        }

        if (!osName.contains("linux")) {
            throw new RuntimeException("Unsupported platform: " + originalOSName);
        }
        return IntelRAPLSensor.instance;
    }
}
