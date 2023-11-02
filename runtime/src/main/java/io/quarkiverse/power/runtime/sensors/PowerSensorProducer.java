package io.quarkiverse.power.runtime.sensors;

import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;

import io.quarkiverse.power.runtime.sensors.linux.rapl.IntelRAPLSensor;
import io.quarkiverse.power.runtime.sensors.macos.powermetrics.MacOSPowermetricsSensor;

@Singleton
public class PowerSensorProducer {
    @Produces
    public PowerSensor<?> sensor() {
        return determinePowerSensor();
    }

    public static PowerSensor<? extends IncrementableMeasure> determinePowerSensor() {
        final var originalOSName = System.getProperty("os.name");
        String osName = originalOSName.toLowerCase();

        if (osName.contains("mac os x")) {
            return new MacOSPowermetricsSensor();
        }

        if (!osName.contains("linux")) {
            throw new RuntimeException("Unsupported platform: " + originalOSName);
        }
        return new IntelRAPLSensor();
    }
}
