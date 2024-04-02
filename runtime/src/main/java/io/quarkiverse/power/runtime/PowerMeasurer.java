package io.quarkiverse.power.runtime;

import com.sun.management.OperatingSystemMXBean;

import java.lang.management.ManagementFactory;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

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
    private Consumer<PowerMeasure> completed;
    private BiConsumer<Integer, PowerMeasure> sampled;
    private Consumer<Exception> errorHandler;
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
        this.onError(null);
    }

    public static double cpuShareOfJVMProcess() {
        final var processCpuLoad = osBean.getProcessCpuLoad();
        final var cpuLoad = osBean.getCpuLoad();
        return (processCpuLoad < 0 || cpuLoad <= 0) ? 0 : processCpuLoad / cpuLoad;
    }

    public void onCompleted(Consumer<PowerMeasure> completed) {
        this.completed = completed;
    }

    public void onSampled(BiConsumer<Integer, PowerMeasure> sampled) {
        this.sampled = sampled;
    }

    public void onError(Consumer<Exception> errorHandler) {
        this.errorHandler = errorHandler != null ? errorHandler : (exception) -> {
            throw new RuntimeException(exception);
        };
    }

    public Optional<String> additionalSensorInfo() {
        //        return Optional.ofNullable(measure).flatMap(sensor::additionalInfo);
        return Optional.empty(); // fixme
    }

    public boolean isRunning() {
        return sampler.isRunning();
    }

    public void start(long durationInSeconds, long frequencyInMilliseconds)
            throws Exception {
        try {
            sampler.start(durationInSeconds, frequencyInMilliseconds);

            if (durationInSeconds > 0) {
                executor.schedule(this::stop, durationInSeconds, TimeUnit.SECONDS);
            }
        } catch (Exception e) {
            handleError(e);
        }
    }

    private void handleError(Exception e) {
        errorHandler.accept(e);
        try {
            sampler.stop(completed);
        } catch (Exception ex) {
            // ignore shutting down exceptions
        }
    }

    public void stop() {
        try {
            if (isRunning()) {
                sampler.stop(completed);
            }
        } catch (Exception e) {
            handleError(e);
        }
    }
}
