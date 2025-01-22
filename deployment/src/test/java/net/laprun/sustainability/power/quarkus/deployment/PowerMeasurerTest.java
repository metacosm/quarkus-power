package net.laprun.sustainability.power.quarkus.deployment;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URI;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.test.QuarkusUnitTest;
import io.quarkus.test.common.http.TestHTTPResource;
import net.laprun.sustainability.power.PowerResource;
import net.laprun.sustainability.power.quarkus.runtime.PowerMeasurer;
import net.laprun.sustainability.power.quarkus.runtime.ServerSampler;

public class PowerMeasurerTest {
    @TestHTTPResource
    URI uri;

    @RegisterExtension
    static final QuarkusUnitTest config = new QuarkusUnitTest()
            .withApplicationRoot((jar) -> jar.addClasses(PowerResource.class, TestPowerMeasurer.class, TestPowerSensor.class));

    @Test
    void startShouldAccumulateOverSpecifiedDurationAndStop() {
        final var measurer = new PowerMeasurer(new ServerSampler(uri));

        measurer.start(1, 100);
        measurer.withCompletedHandler(measure -> assertEquals(10, measure.getSamplesCount()));
    }
}
