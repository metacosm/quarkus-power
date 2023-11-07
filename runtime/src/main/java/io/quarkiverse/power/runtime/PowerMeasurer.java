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
    private StoppedPowerMeasure<M> baseline;

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

    public void start(long durationInSeconds, long frequencyInMilliseconds, PowerSensor.Writer out) throws Exception {
        start(durationInSeconds, frequencyInMilliseconds, false, out);
    }

    void start(long durationInSeconds, long frequencyInMilliseconds, boolean skipBaseline, PowerSensor.Writer out)
            throws Exception {
        if (!isRunning()) {
            if (!skipBaseline && baseline == null) {
                out.println("Establishing baseline for 30s, please do not use your application until done.");
                out.println("Power measurement will start as configured after this initial measure is done.");
                doStart(30, 1000, out, () -> baselineDone(durationInSeconds, frequencyInMilliseconds, out));
            } else {
                doStart(durationInSeconds, frequencyInMilliseconds, out, () -> stop(out));
            }

        }
    }

    private void doStart(long duration, long frequency, PowerSensor.Writer out, Runnable doneAction) throws Exception {
        measure = sensor.start(duration, frequency, out);

        if (duration > 0) {
            executor.schedule(doneAction, duration, TimeUnit.SECONDS);
        }

        scheduled = executor.scheduleAtFixedRate(() -> update(out),
                0, frequency,
                TimeUnit.MILLISECONDS);
    }

    private void update(PowerSensor.Writer out) {
        sensor.update(measure, out);
        measure.incrementSamples();
    }

    public void stop(PowerSensor.Writer out) {
        if (isRunning()) {
            sensor.stop();
            scheduled.cancel(true);
            outputConsumptionSinceStarted(out, false);
            lastMeasure = new StoppedPowerMeasure<>(measure);
            measure = null;
        }
    }

    private void baselineDone(long durationInSeconds, long frequencyInMilliseconds, PowerSensor.Writer out) {
        if (isRunning()) {
            sensor.stop();
            scheduled.cancel(true);
            outputConsumptionSinceStarted(out, true);
            baseline = new StoppedPowerMeasure<>(measure);
            out.println("Baseline established! You can now interact with your application normally.");
            measure = null;
            try {
                doStart(durationInSeconds, frequencyInMilliseconds, out, () -> stop(out));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
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

    private void outputConsumptionSinceStarted(PowerSensor.Writer out, boolean isBaseline) {
        out = out == null ? System.out::println : out;
        final var durationInSeconds = measure.duration() / 1000;
        final var title = isBaseline ? "Baseline power: " : "Measured power: ";
        out.println(title + getReadablePower(measure) + " over " + durationInSeconds
                + " seconds (" + measure.numberOfSamples() + " samples)");
        if (!isBaseline) {
            sensor.additionalInfo(measure, out);
            out.println("Baseline power was " + getReadablePower(baseline));
        }
    }

    private static String getReadablePower(PowerMeasure<?> measure) {
        final var measuredMilliWatts = measure.total();
        return measuredMilliWatts >= 1000 ? (measuredMilliWatts / 1000) + " W" : measuredMilliWatts + "mW";
    }
}
