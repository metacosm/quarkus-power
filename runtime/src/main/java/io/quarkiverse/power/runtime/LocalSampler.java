package io.quarkiverse.power.runtime;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import io.quarkiverse.power.runtime.sensors.OngoingPowerMeasure;
import io.quarkiverse.power.runtime.sensors.PowerSensor;
import io.quarkiverse.power.runtime.sensors.StoppedPowerMeasure;

public class LocalSampler implements Sampler {
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> scheduled;
    private final PowerSensor<?> sensor;
    private OngoingPowerMeasure measure;

    public LocalSampler(PowerSensor<?> sensor) {
        this.sensor = sensor;
    }

    @Override
    public boolean isRunning() {
        return measure != null;
    }

    public void start(long durationInSeconds, long frequencyInMilliseconds) throws Exception {
        measure = sensor.start(durationInSeconds, frequencyInMilliseconds);
        scheduled = executor.scheduleAtFixedRate(this::update, 0, frequencyInMilliseconds, TimeUnit.MILLISECONDS);
    }

    private void update() {
        try {
            sensor.update(measure);
            //            if (this.sampled != null) {
            //                sampled.accept(measure.numberOfSamples(), measure);
            //            }
        } catch (Exception e) {
            //            handleError(e); // fixme
            throw new RuntimeException(e);
        }
    }

    public void stop(Consumer<PowerMeasure> completed) {
        if (scheduled != null) {
            scheduled.cancel(true);
        }
        if (sensor != null) {
            sensor.stop();
        }
        final var measured = new StoppedPowerMeasure(measure);
        measure = null;
        completed.accept(measured);
    }
}
