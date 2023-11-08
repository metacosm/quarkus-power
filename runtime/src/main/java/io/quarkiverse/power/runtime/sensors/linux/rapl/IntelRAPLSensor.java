package io.quarkiverse.power.runtime.sensors.linux.rapl;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import io.quarkiverse.power.runtime.PowerMeasurer;
import io.quarkiverse.power.runtime.sensors.OngoingPowerMeasure;
import io.quarkiverse.power.runtime.sensors.PowerSensor;

public class IntelRAPLSensor implements PowerSensor<IntelRAPLMeasure> {
    private final Map<String, RAPLFile> raplFiles = new HashMap<>();
    private long frequency;

    interface RAPLFile {
        long extractPowerMeasure();

        static RAPLFile createFrom(Path file) {
            return ByteBufferRAPLFile.createFrom(file);
        }
    }

    static class ByteBufferRAPLFile implements RAPLFile {
        private static final int CAPACITY = 64;
        private final ByteBuffer buffer;
        private final FileChannel channel;

        private ByteBufferRAPLFile(FileChannel channel) {
            this.channel = channel;
            buffer = ByteBuffer.allocate(CAPACITY);
        }

        static RAPLFile createFrom(Path file) {
            try {
                return new ByteBufferRAPLFile(new RandomAccessFile(file.toFile(), "r").getChannel());
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }

        public long extractPowerMeasure() {
            try {
                channel.read(buffer);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            long value = 0;
            // will work even better if we can hard code as a static final const the length, in case won't change or is defined by spec
            for (int i = 0; i < CAPACITY; i++) {
                byte digit = buffer.get(i);
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
                raplFiles.put(name, RAPLFile.createFrom(raplFile));
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
    public OngoingPowerMeasure<IntelRAPLMeasure> start(long duration, long frequency) throws Exception {
        this.frequency = frequency;
        IntelRAPLMeasure measure = new IntelRAPLMeasure();
        update(measure);
        return new OngoingPowerMeasure<>(measure);
    }

    private void update(IntelRAPLMeasure measure) {
        double cpuShare = PowerMeasurer.instance().cpuShareOfJVMProcess();
        raplFiles.forEach((name, buffer) -> measure.updateValue(name, buffer.extractPowerMeasure(), cpuShare, frequency));
    }

    @Override
    public void update(OngoingPowerMeasure<IntelRAPLMeasure> ongoingMeasure) {
        update(ongoingMeasure.sensorMeasure());
    }
}
