package io.quarkiverse.power.runtime.sensors.linux.rapl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import io.quarkiverse.power.runtime.PowerMeasurer;

public class IntelRAPLSensorTest {
    @Test
    void extractPowerMeasure() {
        final PowerMeasurer<?> measurer = Mockito.mock(PowerMeasurer.class);
        when(measurer.cpuShareOfJVMProcess())
                .thenReturn(0.5)
                .thenReturn(0.3)
                .thenReturn(0.1);

        assertEquals(10000 * 0.5 + 20000 * 0.3 + 30000 * 0.1,
                IntelRAPLSensor.extractPowerMeasure(Stream.of(10000L, 20000L, 30000L), measurer));
    }
}
