package io.quarkiverse.power.runtime.sensors.linux.rapl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import io.quarkiverse.power.runtime.PowerSensor;

public class IntelRAPLSensor implements PowerSensor<IntelRAPLMeasure> {

    public static final IntelRAPLSensor instance = new IntelRAPLSensor();

    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> scheduled;

    private final List<Path> raplFiles = new ArrayList<>(3);

    private IntelRAPLMeasure accumulatedPower;
    private boolean running;

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
    public void start(long duration, long frequency, Writer out) throws IOException, Exception {
        if (!running) {
            accumulatedPower = new IntelRAPLMeasure(extractPowerMeasure());
            running = true;

            if (duration > 0) {
                executor.schedule(() -> stop(out), duration, TimeUnit.SECONDS);
            }

            scheduled = executor.scheduleAtFixedRate(
                    this::accumulatePower,
                    0, frequency,
                    TimeUnit.MILLISECONDS);
        }
    }

    private void stop(Writer out) {
        stop();
        outputConsumptionSinceStarted(out);
    }

    private void accumulatePower() {
        accumulatedPower.addCPU(extractPowerMeasure());
        accumulatedPower.incrementSamples();
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

    @Override
    public IntelRAPLMeasure stop() {
        if (running) {
            scheduled.cancel(true);
        }
        running = false;
        return accumulatedPower;
    }

    @Override
    public void outputConsumptionSinceStarted(Writer out) {
        out = out == null ? System.out::println : out;
        out.println("Consumed " + accumulatedPower.total() + " mW over " + (accumulatedPower.measureDuration() / 1000)
                + " seconds (" + accumulatedPower.numberOfSamples() + " samples)");
    }
}
