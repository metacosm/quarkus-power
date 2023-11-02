package io.quarkiverse.power.runtime.sensors.linux.rapl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import io.quarkiverse.power.runtime.PowerMeasurer;
import io.quarkiverse.power.runtime.SensorMeasure;
import io.quarkiverse.power.runtime.sensors.OngoingPowerMeasure;
import io.quarkiverse.power.runtime.sensors.PowerSensor;

public class IntelRAPLSensor implements PowerSensor<IntelRAPLMeasure> {
    private final List<Path> raplFiles = new ArrayList<>(3);

    public IntelRAPLSensor() {
        // if we total system energy is not available, read package and DRAM if possible
        // todo: extract more granular information
        // todo: check Intel doc
        if (!checkAvailablity("/sys/class/powercap/intel-rapl/intel-rapl:1/energy_uj")) {
            checkAvailablity("/sys/class/powercap/intel-rapl/intel-rapl:0/energy_uj");
            checkAvailablity("/sys/class/powercap/intel-rapl/intel-rapl:0/intel-rapl:0:2/energy_uj");
        }

        if (raplFiles.isEmpty()) {
            throw new RuntimeException("Failed to get RAPL energy readings, probably due to lack of read access ");
        }
    }

    private boolean checkAvailablity(String raplFileAsString) {
        final var raplFile = Path.of(raplFileAsString);
        if (Files.exists(raplFile) && Files.isReadable(raplFile)) {
            raplFiles.add(raplFile);
            return true;
        }
        return false;
    }

    @Override
    public OngoingPowerMeasure<IntelRAPLMeasure> start(long duration, long frequency, Writer out) throws Exception {
        return new OngoingPowerMeasure<>(new IntelRAPLMeasure(extractPowerMeasure()));
    }

    @Override
    public void update(OngoingPowerMeasure<IntelRAPLMeasure> ongoingMeasure, Writer out) {
        ongoingMeasure.addCPU(extractPowerMeasure());
    }

    private double extractPowerMeasure() {
        return extractPowerMeasure(raplFiles.stream().map(IntelRAPLSensor::readLongFromFile), PowerMeasurer.instance());
    }

    private static long readLongFromFile(Path f) {
        try {
            return Long.parseLong(Files.readString(f).trim());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static double extractPowerMeasure(Stream<Long> measures, PowerMeasurer<? extends SensorMeasure> measurer) {
        return measures
                // first compute attributed power based on CPU share for each measure
                .map(measure -> measure * measurer.cpuShareOfJVMProcess())
                // then sum
                .reduce(Double::sum)
                .orElse(0.0);
    }
}
