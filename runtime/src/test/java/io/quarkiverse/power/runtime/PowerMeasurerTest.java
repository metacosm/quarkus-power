package io.quarkiverse.power.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import io.quarkiverse.power.runtime.sensors.IncrementableMeasure;
import io.quarkiverse.power.runtime.sensors.PowerSensor;
import io.quarkiverse.power.runtime.sensors.PowerSensorProducer;

public class PowerMeasurerTest {
    @Test
    void startShouldAccumulateOverSpecifiedDurationAndStop() throws Exception {
        PowerSensor<? extends IncrementableMeasure> sensor = PowerSensorProducer.determinePowerSensor();
        sensor = Mockito.spy(sensor);
        final var measurer = new PowerMeasurer<>(sensor);

        measurer.start(1, 100, null);
        Thread.sleep(2000);
        final var measure = measurer.current();
        assertEquals(10, measure.numberOfSamples());
        Mockito.verify(sensor, Mockito.times(10)).update(Mockito.any(), Mockito.any());
    }
}
