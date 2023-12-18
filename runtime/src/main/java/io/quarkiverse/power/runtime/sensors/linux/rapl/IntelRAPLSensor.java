package io.quarkiverse.power.runtime.sensors.linux.rapl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.SortedMap;
import java.util.TreeMap;

import io.quarkiverse.power.runtime.PowerMeasurer;
import io.quarkiverse.power.runtime.SensorMetadata;
import io.quarkiverse.power.runtime.sensors.OngoingPowerMeasure;
import io.quarkiverse.power.runtime.sensors.PowerSensor;

public class IntelRAPLSensor implements PowerSensor<IntelRAPLMeasure> {
    private final RAPLFile[] raplFiles;
    private final SensorMetadata metadata;
    private final double[] lastMeasuredSensorValues;
    private long frequency;

    public IntelRAPLSensor() {
        // if we total system energy is not available, read package and DRAM if possible
        // todo: check Intel doc
        final var files = new TreeMap<String, RAPLFile>();
        if (!addFileIfReadable("/sys/class/powercap/intel-rapl/intel-rapl:1/energy_uj", files)) {
            addFileIfReadable("/sys/class/powercap/intel-rapl/intel-rapl:0/energy_uj", files);
            addFileIfReadable("/sys/class/powercap/intel-rapl/intel-rapl:0/intel-rapl:0:2/energy_uj", files);
        }

        if (files.isEmpty()) {
            throw new RuntimeException("Failed to get RAPL energy readings, probably due to lack of read access ");
        }

        raplFiles = files.values().toArray(new RAPLFile[0]);
        final var metadata = new HashMap<String, Integer>(files.size());
        int fileNb = 0;
        for (String name : files.keySet()) {
            metadata.put(name, fileNb++);
        }
        this.metadata = new SensorMetadata() {
            @Override
            public int indexFor(String component) {
                final var index = metadata.get(component);
                if (index == null) {
                    throw new IllegalArgumentException("Unknow component: " + component);
                }
                return index;
            }

            @Override
            public int componentCardinality() {
                return metadata.size();
            }
        };
        lastMeasuredSensorValues = new double[raplFiles.length];
    }

    private boolean addFileIfReadable(String raplFileAsString, SortedMap<String, RAPLFile> files) {
        final var raplFile = Path.of(raplFileAsString);
        if (isReadable(raplFile)) {
            // get metric name
            final var nameFile = raplFile.resolveSibling("name");
            if (!isReadable(nameFile)) {
                throw new IllegalStateException("No name associated with " + raplFileAsString);
            }

            try {
                final var name = Files.readString(nameFile).trim();
                files.put(name, RAPLFile.createFrom(raplFile));
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }
        return false;
    }

    private static boolean isReadable(Path file) {
        return Files.exists(file) && Files.isReadable(file);
    }

    @Override
    public OngoingPowerMeasure start(long duration, long frequency) throws Exception {
        this.frequency = frequency;

        // perform an initial measure to prime the data
        final var ongoingMeasure = new OngoingPowerMeasure(metadata, duration, frequency);
        update(ongoingMeasure);
        return ongoingMeasure;
    }

    private double computeNewComponentValue(int componentIndex, long sensorValue, double cpuShare) {
        return (sensorValue - lastMeasuredSensorValues[componentIndex]) * cpuShare / frequency / 1000;
    }

    @Override
    public void update(OngoingPowerMeasure ongoingMeasure) {
        double cpuShare = PowerMeasurer.instance().cpuShareOfJVMProcess();
        for (int i = 0; i < raplFiles.length; i++) {
            final var value = raplFiles[i].extractPowerMeasure();
            final var newComponentValue = computeNewComponentValue(i, value, cpuShare);
            lastMeasuredSensorValues[i] = newComponentValue;
        }
        ongoingMeasure.recordMeasure(lastMeasuredSensorValues);
    }

    @Override
    public IntelRAPLMeasure measureFor(double[] measureComponents) {
        return new IntelRAPLMeasure(metadata, measureComponents);
    }
}
