package io.quarkiverse.power.runtime;

import java.io.*;
import java.util.concurrent.*;

public class MacOSPowermetricsSensor implements PowerSensor<AppleSiliconMeasure> {
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    private Process powermetrics;
    private ScheduledFuture<?> powermetricsSchedule;

    private boolean running;
    private AppleSiliconMeasure accumulatedPower = new AppleSiliconMeasure();
    public static PowerSensor<AppleSiliconMeasure> instance = new MacOSPowermetricsSensor();
    private final static String pid = " " + ProcessHandle.current().pid() + " ";

    private static class ProcessRecord {
        final double cpu;
        final double gpu;

        public ProcessRecord(String line) {
            //Name                               ID     CPU ms/s  samp ms/s  User%  Deadlines (<2 ms, 2-5 ms)  Wakeups (Intr, Pkg idle)  GPU ms/s
            //iTerm2                             1008   46.66     46.91      83.94  0.00    0.00               30.46   0.00              0.00
            final var processData = line.split("\\s+");
            cpu = Double.parseDouble(processData[3]);
            gpu = Double.parseDouble(processData[9]);
        }
    }

    AppleSiliconMeasure extractPowerMeasure(InputStream powerMeasureInput, long pid) {
        return extractPowerMeasure(powerMeasureInput, " " + pid + " ");
    }

    AppleSiliconMeasure extractPowerMeasure(InputStream powerMeasureInput, String paddedPIDAsString) {
        try {
            // Should not be closed since it closes the process
            BufferedReader input = new BufferedReader(new InputStreamReader(powerMeasureInput));
            String line;
            double cpuShare = -1, gpuShare = -1;
            boolean totalDone = false;
            boolean cpuDone = false;
            while ((line = input.readLine()) != null) {
                if (line.isEmpty() || line.startsWith("*")) {
                    continue;
                }

                // first, look for process line detailing share
                if (cpuShare < 0) {
                    if (line.contains(paddedPIDAsString)) {
                        final var procInfo = new ProcessRecord(line);
                        cpuShare = procInfo.cpu;
                        gpuShare = procInfo.gpu;
                    }
                    continue;
                }

                if (!totalDone) {
                    // then skip all lines until we get the totals
                    if (line.startsWith("ALL_TASKS")) {
                        final var totals = new ProcessRecord(line);
                        // compute ratio
                        cpuShare = cpuShare / totals.cpu;
                        gpuShare = totals.gpu > 0 ? gpuShare / totals.gpu : 0;
                        totalDone = true;
                    }
                    continue;
                }

                if (!cpuDone) {
                    // look for line that contains CPU power measure
                    if (line.startsWith("CPU Power")) {
                        accumulatedPower.addCPU(extractAttributedMeasure(line, cpuShare));
                        cpuDone = true;
                    }
                    continue;
                }

                if (line.startsWith("GPU Power")) {
                    accumulatedPower.addGPU(extractAttributedMeasure(line, gpuShare));
                    continue;
                }

                if (line.startsWith("ANE Power")) {
                    accumulatedPower.addANE(extractAttributedMeasure(line, 1));
                    break;
                }
            }

            accumulatedPower.incrementSamples();
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
        return accumulatedPower;
    }

    private static double extractAttributedMeasure(String line, double attributionRatio) {
        final var powerValue = line.split(":")[1];
        final var powerInMilliwatts = powerValue.split("m")[0];
        return Double.parseDouble(powerInMilliwatts) * attributionRatio;
    }

    @Override
    public void start(long duration, long frequency, final Writer out) throws Exception {
        if (!running) {
            final var freq = Long.toString(Math.round(frequency));
            powermetrics = Runtime.getRuntime()
                    .exec("sudo powermetrics --samplers cpu_power,tasks --show-process-samp-norm --show-process-gpu -i "
                            + freq);

            accumulatedPower = new AppleSiliconMeasure();
            running = true;

            if (duration > 0) {
                executor.schedule(() -> stop(out), duration, TimeUnit.SECONDS);
            }

            powermetricsSchedule = executor.scheduleAtFixedRate(() -> extractPowerMeasure(powermetrics.getInputStream(), pid),
                    0, frequency,
                    TimeUnit.MILLISECONDS);
        }
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
    public void outputConsommation(Writer out) {
        out = out == null ? System.out::println : out;
        out.println("Consumed " + accumulatedPower.total() + " mW");
    }

    private void stop(Writer out) {
        stop();
        outputConsommation(out);
    }
}
