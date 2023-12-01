package io.quarkiverse.power.runtime;

import java.util.function.Consumer;

import io.quarkiverse.power.runtime.sensors.PowerSensorProducer;

public class DualSampler implements Sampler {
    private final LocalSampler local = new LocalSampler(PowerSensorProducer.determinePowerSensor());
    private final ServerSampler server = new ServerSampler();

    @Override
    public boolean isRunning() {
        return local.isRunning() && server.isRunning();
    }

    @Override
    public void start(long durationInSeconds, long frequencyInMilliseconds) throws Exception {
        local.start(durationInSeconds, frequencyInMilliseconds);
        server.start(durationInSeconds, frequencyInMilliseconds);
    }

    @Override
    public void stop(Consumer<PowerMeasure> completed) {
        local.stop(completed);
        server.stop(completed);
    }
}
