package io.quarkiverse.power.runtime;

public interface SensorMetadata {
    int indexFor(String component);

    int componentCardinality();
}
