package io.quarkiverse.power.deployment;

import io.quarkiverse.power.runtime.PowerMeasurer;
import io.quarkiverse.power.runtime.ServerSampler;
import io.quarkus.test.QuarkusUnitTest;
import io.quarkus.test.common.http.TestHTTPResource;
import net.laprun.sustainability.power.PowerResource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PowerMeasurerTest {
    @TestHTTPResource
    URI uri;

    @RegisterExtension
    static final QuarkusUnitTest config = new QuarkusUnitTest()
            .withApplicationRoot((jar) -> jar.addClasses(PowerResource.class, TestPowerMeasurer.class, TestPowerSensor.class));

    @Test
    void startShouldAccumulateOverSpecifiedDurationAndStop() throws Exception {
        final var measurer = new PowerMeasurer(new ServerSampler(uri));

        measurer.start(1, 100);
        measurer.onCompleted(measure -> assertEquals(10, measure.numberOfSamples()));
    }
}
