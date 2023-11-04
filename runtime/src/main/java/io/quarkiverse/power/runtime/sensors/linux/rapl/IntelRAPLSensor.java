package io.quarkiverse.power.runtime.sensors.linux.rapl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import io.quarkiverse.power.runtime.PowerMeasurer;
import io.quarkiverse.power.runtime.SensorMeasure;
import io.quarkiverse.power.runtime.sensors.OngoingPowerMeasure;
import io.quarkiverse.power.runtime.sensors.PowerSensor;

public class IntelRAPLSensor implements PowerSensor<IntelRAPLMeasure> {
    private final Map<String, ByteBuffer> raplFiles = new HashMap<>();
    private double frequency;

    public IntelRAPLSensor() {
        // if we total system energy is not available, read package and DRAM if possible
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
        if (isReadable(raplFile)) {
            // get metric name
            final var nameFile = raplFile.resolveSibling("name");
            if (!isReadable(nameFile)) {
                throw new IllegalStateException("No name associated with " + raplFileAsString);
            }

            try {
                final var name = Files.readString(nameFile).trim();
                raplFiles.put(name, mapRaplFile(raplFile));
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
    public OngoingPowerMeasure<IntelRAPLMeasure> start(long duration, long frequency, Writer out) throws Exception {
        this.frequency = (double) frequency / 1000;
        IntelRAPLMeasure measure = new IntelRAPLMeasure();
        update(measure);
        return new OngoingPowerMeasure<>(measure);
    }

    private void update(IntelRAPLMeasure measure) {
        double cpuShare = PowerMeasurer.instance().cpuShareOfJVMProcess();
        raplFiles.forEach((name, buffer) -> measure.updateValue(name, extractPowerMeasure(buffer), cpuShare, frequency));
    }

    @Override
    public void update(OngoingPowerMeasure<IntelRAPLMeasure> ongoingMeasure, Writer out) {
       update(ongoingMeasure.sensorMeasure());
    }

    private static ByteBuffer mapRaplFile(Path file) throws IOException {
        try (var channel = FileChannel.open(file, StandardOpenOption.READ)) {
            ByteBuffer buffer = ByteBuffer.allocate((int) channel.size());
            channel.read(buffer);
            return buffer;
        }
    }

    private static long extractPowerMeasure(ByteBuffer raplBuffer) {
        long value = 0;
        // will work even better if we can hard code as a static final const the length, in case won't change or is defined by spec
        for (int i = 0; i < raplBuffer.capacity(); i++) {
            byte digit = raplBuffer.get(i);
            if (digit >= '0' && digit <= '9') {
                value = value * 10 + (digit - '0');
            } else {
                if (digit == '\n') {
                    return value;
                }
                // Invalid character; handle accordingly or throw an exception
                throw new NumberFormatException("Invalid character in input: '" + Character.toString(digit) + "'");
            }
        }
        return value;
    }
}
