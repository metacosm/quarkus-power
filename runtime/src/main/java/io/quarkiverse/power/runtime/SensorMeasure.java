package io.quarkiverse.power.runtime;

import java.util.Optional;

public interface SensorMeasure {
    String CPU = "cpu";
    String GPU = "gpu";
    String TOTAL = "total";

    double cpu();

    default Optional<Double> gpu() {
        return Optional.empty();
    }

    default Optional<Double> byKey(String key) {
        return switch (key) {
            case CPU -> Optional.of(cpu());
            case GPU -> gpu();
            case TOTAL -> Optional.of(total());
            default -> Optional.empty();
        };
    }

    default double total() {
        return cpu() + gpu().orElse(0.0);
    }
}
