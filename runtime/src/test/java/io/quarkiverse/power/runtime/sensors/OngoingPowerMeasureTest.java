package io.quarkiverse.power.runtime.sensors;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import io.quarkiverse.power.runtime.SensorMetadata;

public class OngoingPowerMeasureTest {

    @Test
    public void testStandardDeviation() {
        final var m1c1 = 10.0;
        final var m1c2 = 12.0;
        final var m2c1 = 8.0;
        final var m2c2 = 17.0;
        final var metadata = new SensorMetadata() {
            @Override
            public int indexFor(String component) {
                throw new UnsupportedOperationException("not needed for testing");
            }

            @Override
            public int componentCardinality() {
                return 2;
            }
        };
        final var measure = new OngoingPowerMeasure(metadata, 1, 500);

        final var components = new double[metadata.componentCardinality()];
        components[0] = m1c1;
        components[1] = m1c2;
        measure.recordMeasure(components);

        components[0] = m2c1;
        components[1] = m2c2;
        measure.recordMeasure(components);

        assertEquals(m1c1 + m1c2 + m2c1 + m2c2, measure.total());
        assertEquals((m1c1 + m1c2 + m2c1 + m2c2) / 2, measure.average());
        assertEquals(Math.min(m1c1 + m1c2, m2c1 + m2c2), measure.minMeasuredTotal());
        assertEquals(Math.max(m1c1 + m1c2, m2c1 + m2c2), measure.maxMeasuredTotal());
        final var c1Avg = measure.averagesPerComponent()[0];
        final var c2Avg = measure.averagesPerComponent()[1];
        assertEquals((m1c1 + m2c1) / 2, c1Avg);
        assertEquals((m1c2 + m2c2) / 2, c2Avg);

        final var stdVarForC1 = (Math.pow(m1c1 - c1Avg, 2) + Math.pow(m2c1 - c1Avg, 2)) / 1;
        final var stdVarForC2 = (Math.pow(m1c2 - c2Avg, 2) + Math.pow(m2c2 - c2Avg, 2)) / 1;
        double expectedStandardDeviation = Math.sqrt((stdVarForC1 + stdVarForC2) / 2);

        assertEquals(expectedStandardDeviation, measure.standardDeviation(), 0.0001,
                "Standard Deviation did not match the expected value");
    }
}
