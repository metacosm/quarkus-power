package net.laprun.sustainability.power.quarkus.runtime;

import java.lang.management.ManagementFactory;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import com.sun.management.OperatingSystemMXBean;

import net.laprun.sustainability.power.SensorMetadata;
import net.laprun.sustainability.power.measure.PowerMeasure;

public class PowerMeasurer {
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
    private final Sampler sampler;
    private static PowerMeasurer instance;

    public static PowerMeasurer instance() {
        if (instance == null) {
            instance = new PowerMeasurer();
        }
        return instance;
    }

    private PowerMeasurer() {
        this(new ServerSampler());
    }

    public PowerMeasurer(Sampler sampler) {
        this.sampler = sampler;
        this.withErrorHandler(null);
    }

    public Sampler sampler() {
        return sampler;
    }

    @SuppressWarnings("unused")
    public static double cpuShareOfJVMProcess() {
        final var processCpuLoad = osBean.getProcessCpuLoad();
        final var cpuLoad = osBean.getCpuLoad();
        return (processCpuLoad < 0 || cpuLoad <= 0) ? 0 : processCpuLoad / cpuLoad;
    }

    public PowerMeasurer withCompletedHandler(Consumer<PowerMeasure> completed) {
        sampler.withCompletedHandler(completed);
        return this;
    }

    public PowerMeasurer withErrorHandler(Consumer<Throwable> errorHandler) {
        errorHandler = errorHandler != null ? errorHandler : (exception) -> {
            throw new RuntimeException(exception);
        };
        sampler.withErrorHandler(errorHandler);
        return this;
    }

    public SensorMetadata metadata() {
        return sampler.metadata();
    }

    public boolean isRunning() {
        return sampler.isRunning();
    }

    public void start(long durationInSeconds, long frequencyInMilliseconds) {
        sampler.start(durationInSeconds, frequencyInMilliseconds);

        if (durationInSeconds > 0) {
            executor.schedule(this::stop, durationInSeconds, TimeUnit.SECONDS);
        }
    }

    public void stop() {
        if (isRunning()) {
            sampler.stop();
        }
    }
}
