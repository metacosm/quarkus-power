package io.quarkiverse.power.runtime.sensors.macos.jmx;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.sun.management.OperatingSystemMXBean;

import io.quarkiverse.power.runtime.PowerSensor;
import io.quarkiverse.power.runtime.sensors.macos.AppleSiliconMeasure;

public class JMXCPUSensor implements PowerSensor<AppleSiliconMeasure> {
    private static final OperatingSystemMXBean osBean;
    static {
        osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
    }

    public static PowerSensor<AppleSiliconMeasure> instance = new JMXCPUSensor();

    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    private Process powermetrics;
    private ScheduledFuture<?> powermetricsSchedule;

    private boolean running;
    private AppleSiliconMeasure accumulatedPower = new AppleSiliconMeasure();

    @Override
    public void start(long duration, long frequency, Writer out) throws IOException, Exception {
        if (!running) {
            final var freq = Long.toString(Math.round(frequency));
            powermetrics = Runtime.getRuntime().exec("sudo powermetrics --samplers cpu_power -i " + freq);

            accumulatedPower = new AppleSiliconMeasure();
            running = true;

            if (duration > 0) {
                executor.schedule(() -> stop(out), duration, TimeUnit.SECONDS);
            }

            powermetricsSchedule = executor.scheduleAtFixedRate(() -> extractPowerMeasure(powermetrics.getInputStream()),
                    0, frequency,
                    TimeUnit.MILLISECONDS);
        }
    }

    void extractPowerMeasure(InputStream powerMeasureInput) {
        try {
            // Should not be closed since it closes the process
            BufferedReader input = new BufferedReader(new InputStreamReader(powerMeasureInput));
            String line;
            while ((line = input.readLine()) != null) {
                if (line.isEmpty() || line.startsWith("*")) {
                    continue;
                }

                // look for line that contains CPU power measure
                if (line.startsWith("CPU Power")) {
                    final var processCpuLoad = osBean.getProcessCpuLoad();
                    final var cpuLoad = osBean.getCpuLoad();
                    if (processCpuLoad < 0 || cpuLoad <= 0) {
                        break;
                    }
                    final var cpuShare = processCpuLoad / cpuLoad;
                    accumulatedPower.addCPU(extractAttributedMeasure(line, cpuShare));
                    break;
                }
            }

            accumulatedPower.incrementSamples();
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    private static double extractAttributedMeasure(String line, double attributionRatio) {
        final var powerValue = line.split(":")[1];
        final var powerInMilliwatts = powerValue.split("m")[0];
        return Double.parseDouble(powerInMilliwatts) * attributionRatio;
    }

    @Override
    public AppleSiliconMeasure stop() {
        if (running) {
            powermetrics.destroy();
            powermetricsSchedule.cancel(true);
        }
        running = false;
        return accumulatedPower;
    }

    @Override
    public void outputConsumptionSinceStarted(Writer out) {
        out = out == null ? System.out::println : out;
        out.println("Consumed " + accumulatedPower.total() + " mW over " + (accumulatedPower.measureDuration() / 1000)
                + " seconds");
    }

    private void stop(Writer out) {
        stop();
        outputConsumptionSinceStarted(out);
    }
}
