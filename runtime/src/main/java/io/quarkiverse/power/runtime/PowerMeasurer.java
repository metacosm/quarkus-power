package io.quarkiverse.power.runtime;

import java.lang.management.ManagementFactory;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.sun.management.OperatingSystemMXBean;

import io.quarkiverse.power.runtime.sensors.*;

public class PowerMeasurer<M extends IncrementableMeasure> {
    private static final OperatingSystemMXBean osBean;
    static {
        osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        // take two measures to avoid initial zero values
        osBean.getProcessCpuLoad();
        osBean.getCpuLoad();
        osBean.getProcessCpuLoad();
        osBean.getCpuLoad();
    }

    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> scheduled;
    private final PowerSensor<M> sensor;
    private OngoingPowerMeasure<M> measure;
    private StoppedPowerMeasure<M> lastMeasure;

    private final static PowerMeasurer<? extends SensorMeasure> instance = new PowerMeasurer<>(
            PowerSensorProducer.determinePowerSensor());

    public static PowerMeasurer<? extends SensorMeasure> instance() {
        return instance;
    }

    public PowerMeasurer(PowerSensor<M> sensor) {
        this.sensor = sensor;
    }

    public double cpuShareOfJVMProcess() {
        final var processCpuLoad = osBean.getProcessCpuLoad();
        final var cpuLoad = osBean.getCpuLoad();
        return (processCpuLoad < 0 || cpuLoad <= 0) ? 0 : processCpuLoad / cpuLoad;
    }

    PowerSensor<M> sensor() {
        return sensor;
    }

    public boolean isRunning() {
        return measure != null;
    }

    public void start(long duration, long frequency, PowerSensor.Writer out) throws Exception {
        if (!isRunning()) {
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

    public void stop(PowerSensor.Writer out) {
        if (isRunning()) {
            sensor.stop();
            scheduled.cancel(true);
            outputConsumptionSinceStarted(out);
            lastMeasure = new StoppedPowerMeasure<>(measure);
            measure = null;
        }
    }

    public PowerMeasure<M> current() {
        // use the ongoing power measure if it exists
        if (measure == null) {
            // or use the last recorded measure if we have one
            if (lastMeasure != null) {
                return lastMeasure;
            } else {
                throw new IllegalStateException("No power measure found. Please start it first.");
            }
        } else {
            return measure;
        }
    }

    private void outputConsumptionSinceStarted(PowerSensor.Writer out) {
        out = out == null ? System.out::println : out;
        out.println("Consumed " + measure.total() + " mW over " + (measure.duration() / 1000)
                + " seconds (" + measure.numberOfSamples() + " samples)");
        sensor.additionalInfo(measure, out);
    }
}
