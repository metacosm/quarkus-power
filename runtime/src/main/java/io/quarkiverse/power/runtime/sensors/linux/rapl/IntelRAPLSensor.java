package io.quarkiverse.power.runtime.sensors.linux.rapl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import io.quarkiverse.power.runtime.sensors.OngoingPowerMeasure;
import io.quarkiverse.power.runtime.sensors.PowerSensor;

public class IntelRAPLSensor implements PowerSensor<IntelRAPLMeasure> {

    public static final IntelRAPLSensor instance = new IntelRAPLSensor();
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

    private long extractPowerMeasure() {
        long energyData = 0;
        for (final Path raplFile : raplFiles) {
            try {
                energyData += Long.parseLong(Files.readString(raplFile).trim());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return energyData;
    }
}
