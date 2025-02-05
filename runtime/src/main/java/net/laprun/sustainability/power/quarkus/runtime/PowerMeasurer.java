package net.laprun.sustainability.power.quarkus.runtime;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import net.laprun.sustainability.power.SensorMetadata;

@ApplicationScoped
public class PowerMeasurer {
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private final ServerSampler sampler;
    @Inject
    Measures measures;

    public PowerMeasurer() {
        this(new ServerSampler());
    }

    public PowerMeasurer(ServerSampler sampler) {
        this.sampler = sampler;
        this.withErrorHandler(null);
    }

    public ServerSampler sampler() {
        return sampler;
    }

    public <T> Metadata<T> measureMetadata(Function<SensorMetadata.ComponentMetadata, T> converter) {
        final var metadata = sampler.metadata();
        final var local = sampler.localMetadata();
        final var remote = metadata.map(sm -> {
                    var list = sm.components().values().stream().toList();
                    if (!local.isEmpty()) {
                        list = new ArrayList<>(list);
                        list.addAll(local);
                    }
                    return list;
                })
                .orElse(List.of());
        return new Metadata<>(sampler.powerServerURI(),
                metadata.map(SensorMetadata::documentation).orElse(null),
                remote, sampler.status(), converter);
    }

    public PowerMeasurer withCompletedHandler(Consumer<DisplayableMeasure> completed) {
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

    public boolean isRunning() {
        return sampler.isRunning();
    }

    public void start(long durationInSeconds, long frequencyInMilliseconds) {
        if (!isRunning()) {
            sampler.start(durationInSeconds, frequencyInMilliseconds);

            if (durationInSeconds > 0) {
                executor.schedule(this::stop, durationInSeconds, TimeUnit.SECONDS);
            }
        }
    }

    public Optional<DisplayableMeasure> stop() {
        return isRunning() ? Optional.of(sampler.stop()) : Optional.empty();
    }

    public void recordMethodMeasure(String methodKey, String threadName, long threadId, long startTime, Duration duration,
                                    double threadCPU) {
        if (!isRunning()) {
            throw new IllegalStateException("Not running");
        }

        measures.add(methodKey, threadName, threadId, startTime, duration, threadCPU, Platform.cpuShareOfJVMProcess());
    }
}
