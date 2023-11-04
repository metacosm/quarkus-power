package io.quarkiverse.power.runtime.sensors.linux.rapl;

import io.quarkiverse.power.runtime.SensorMeasure;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static io.quarkiverse.power.runtime.SensorMeasure.GPU;
import static org.junit.jupiter.api.Assertions.*;

class IntelRAPLMeasureTest {

    private static final String CPU = "package-0";


    @Test
    void updateValue() {
        final var measure = new IntelRAPLMeasure();
        measure.updateValue(CPU, 10000, 0.5, 100); // add 50 to cpu
        measure.updateValue(GPU, 10000, 0.2, 100); // add 20 to gpu
        measure.updateValue(CPU, 20000, 0.4, 100); // add 40 to cpu
        measure.updateValue(CPU, 30000, 0.3, 100); // add 30 to cpu
        measure.updateValue(GPU, 30000, 0.4, 100); // add 80 to cpu

        final var cpu1 = (10000 * 0.5 / 100);
        final var cpu2 = (20000 - 10000) * 0.4 / 100;
        final var cpu3 = (30000 - 20000) * 0.3 / 100;
        final var gpu1 = 10000 * 0.2 / 100;
        final var gpu2 = (30000 - 10000) * 0.4 / 100;
        assertEquals((cpu1 + cpu2 + cpu3) / 1000, measure.cpu());
        assertEquals((cpu1 + cpu2 + cpu3) / 1000, measure.getValue(CPU));
        assertEquals((gpu1 + gpu2) / 1000, measure.getValue(GPU));
        assertEquals(measure.cpu() + measure.getValue(GPU), measure.total());
        assertEquals(measure.getValue(GPU), measure.gpu().orElseThrow());
        assertEquals(measure.total(), measure.byKey(SensorMeasure.TOTAL).orElseThrow());
    }
}