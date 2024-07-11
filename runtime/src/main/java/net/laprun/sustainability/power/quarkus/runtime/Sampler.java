package net.laprun.sustainability.power.quarkus.runtime;

import net.laprun.sustainability.power.measure.PowerMeasure;

import java.util.function.Consumer;

public interface Sampler {

    boolean isRunning();

    void start(long durationInSeconds, long frequencyInMilliseconds);

    void stop();

    void stopOnError(Throwable e);

    @SuppressWarnings("UnusedReturnValue")
    Sampler withCompletedHandler(Consumer<PowerMeasure> completed);

    @SuppressWarnings("UnusedReturnValue")
    Sampler withErrorHandler(Consumer<Throwable> errorHandler);
}
