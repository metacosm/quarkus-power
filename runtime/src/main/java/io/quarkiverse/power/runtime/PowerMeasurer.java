package io.quarkiverse.power.runtime;

import java.lang.management.ManagementFactory;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.sun.management.OperatingSystemMXBean;

import io.quarkiverse.power.runtime.sensors.IncrementableMeasure;
import io.quarkiverse.power.runtime.sensors.OngoingPowerMeasure;
import io.quarkiverse.power.runtime.sensors.PowerSensor;
import io.quarkiverse.power.runtime.sensors.PowerSensorProducer;

public class PowerMeasurer<M extends IncrementableMeasure> {
    public static final OperatingSystemMXBean osBean;

    static {
        osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
    }

    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> scheduled;
    private final PowerSensor<M> sensor;
    private OngoingPowerMeasure<M> measure;

    private final static PowerMeasurer<? extends SensorMeasure> instance = new PowerMeasurer<>(
            PowerSensorProducer.determinePowerSensor());

    public static PowerMeasurer<? extends SensorMeasure> instance() {
        return instance;
    }

    public PowerMeasurer(PowerSensor<M> sensor) {
        this.sensor = sensor;
    }

    public void start(long duration, long frequency, PowerSensor.Writer out) throws Exception {
        if (measure == null) {
            measure = sensor.start(duration, frequency, out);

            if (duration > 0) {
                executor.schedule(() -> stop(out), duration, TimeUnit.SECONDS);
            }

            scheduled = executor.scheduleAtFixedRate(() -> update(out),
                    0, frequency,
                    TimeUnit.MILLISECONDS);
        }
    }

    private void update(PowerSensor.Writer out) {
        sensor.update(measure, out);
        measure.incrementSamples();
    }

    public PowerMeasure<M> stop(PowerSensor.Writer out) {
        if (measure != null) {
            sensor.stop();
            scheduled.cancel(true);
        }
        outputConsumptionSinceStarted(out);
        return measure;
    }

    public PowerMeasure<M> current() {
        return measure;
    }

    private void outputConsumptionSinceStarted(PowerSensor.Writer out) {
        out = out == null ? System.out::println : out;
        out.println("Consumed " + measure.total() + " mW over " + (measure.duration() / 1000)
                + " seconds (" + measure.numberOfSamples() + " samples)");
        sensor.additionalInfo(out);
    }
}