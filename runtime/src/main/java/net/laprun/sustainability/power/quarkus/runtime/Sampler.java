package net.laprun.sustainability.power.quarkus.runtime;

import java.util.function.Consumer;

public interface Sampler {

    boolean isRunning();

    void start(long durationInSeconds, long frequencyInMilliseconds) throws Exception;

    void stop(Consumer<PowerMeasure> completed);
}
