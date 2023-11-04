package io.quarkiverse.power.runtime.sensors.linux.rapl;

import io.quarkiverse.power.runtime.SensorMeasure;
import io.quarkiverse.power.runtime.sensors.IncrementableMeasure;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class IntelRAPLMeasure implements IncrementableMeasure {
    private final Map<String, Accumulator> values = new HashMap<>();

    static class Accumulator {
        private double previous;
        private double accumulated;

        public Accumulator recordNewValue(long currentValue, double durationSinceLastRecord, double share) {
            accumulateAttributedDeltaOverDuration(currentValue, durationSinceLastRecord, share);
            previous = currentValue;
            return this;
        }

        private void accumulateAttributedDeltaOverDuration(long currentValue, double durationSinceLastRecord, double share) {
            accumulated += (currentValue - previous) / durationSinceLastRecord * share;
        }

        public double accumulated() {
            return accumulated;
        }
    }

    double getValue(String name) {
        final var value = values.get(name);
        return value == null ? 0.0 : value.accumulated() / 1000;
    }

    void updateValue(String name, long current, double cpuShare, double frequency) {
        values.compute(name, (n, previous) -> Objects.requireNonNullElseGet(previous, Accumulator::new).recordNewValue(current, frequency, cpuShare));
    }

    @Override
    public double cpu() {
        return getValue("package-0");
    }

    @Override
    public Optional<Double> gpu() {
        return Optional.ofNullable(values.get(SensorMeasure.GPU)).map(value -> value.accumulated() / 1000);
    }

    @Override
    public Optional<Double> byKey(String key) {
        final var v = IncrementableMeasure.super.byKey(key); // first get from default implementation
        if(v.isEmpty()) {
            // try local keys
            return Optional.ofNullable(values.get(key)).map(value -> value.accumulated() / 1000);
        } else {
            return v;
        }
    }

    @Override
    public double total() {
        return values.values().stream().map(Accumulator::accumulated).reduce(Double::sum).orElse(0.0) / 1000;
    }

    @Override
    public void addCPU(double v) {
        throw new IllegalStateException("Shouldn't be called");
    }
}
