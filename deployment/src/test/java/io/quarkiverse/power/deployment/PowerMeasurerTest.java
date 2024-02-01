package io.quarkiverse.power.deployment;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URI;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkiverse.power.runtime.PowerMeasurer;
import io.quarkiverse.power.runtime.ServerSampler;
import io.quarkus.test.QuarkusUnitTest;
import io.quarkus.test.common.http.TestHTTPResource;
import net.laprun.sustainability.power.PowerResource;

public class PowerMeasurerTest {
    @TestHTTPResource
    URI uri;

    @RegisterExtension
    static final QuarkusUnitTest config = new QuarkusUnitTest()
            .withApplicationRoot((jar) -> jar.addClasses(PowerResource.class, TestPowerMeasurer.class, TestPowerSensor.class));

    @Test
    void startShouldAccumulateOverSpecifiedDurationAndStop() throws Exception {
        final var measurer = new PowerMeasurer<>(new ServerSampler(uri));

        measurer.start(1, 100);
        measurer.onCompleted(measure -> {
            assertEquals(10, measure.numberOfSamples());
        });
    }
}
