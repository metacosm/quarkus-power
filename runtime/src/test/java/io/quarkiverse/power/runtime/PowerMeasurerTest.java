package io.quarkiverse.power.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class PowerMeasurerTest {
    @Test
    void startShouldAccumulateOverSpecifiedDurationAndStop() throws Exception {
        final var measurer = PowerMeasurer.instance();
        measurer.start(1, 100, null);
        Thread.sleep(2000);
        final var measure = measurer.current();
        assertEquals(10, measure.numberOfSamples());
    }
}
