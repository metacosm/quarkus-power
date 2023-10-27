package io.quarkiverse.power.runtime.sensors.macos.powermetrics;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class MacOSPowermetricsSensorTest {

    @Test
    void extractPowerMeasure() {
        final var sensor = new MacOSPowermetricsSensor();
        final var measure = sensor
                .extractPowerMeasure(Thread.currentThread().getContextClassLoader().getResourceAsStream("foo.txt"), 29419);
        assertEquals(((23.88 / 1222.65) * 211), measure.cpu());
    }

    @Test
    void startShouldAccumulateOverSpecifiedDurationAndStop() throws Exception {
        final var sensor = new MacOSPowermetricsSensor();
        sensor.start(3, 1000, null);
        Thread.sleep(5000);
        final var measure = sensor.stop();
        assertEquals(3, measure.numberOfSamples());
    }
}
