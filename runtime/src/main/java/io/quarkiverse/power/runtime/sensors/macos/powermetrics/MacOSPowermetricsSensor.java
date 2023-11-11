package io.quarkiverse.power.runtime.sensors.macos.powermetrics;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Optional;

import io.quarkiverse.power.runtime.PowerMeasure;
import io.quarkiverse.power.runtime.PowerMeasurer;
import io.quarkiverse.power.runtime.sensors.OngoingPowerMeasure;
import io.quarkiverse.power.runtime.sensors.PowerSensor;
import io.quarkiverse.power.runtime.sensors.macos.AppleSiliconMeasure;

public class MacOSPowermetricsSensor implements PowerSensor<AppleSiliconMeasure> {
    private Process powermetrics;
    private final static String pid = " " + ProcessHandle.current().pid() + " ";
    private double accumulatedCPUShareDiff = 0.0;
    private final int cpu;
    private final int gpu;
    private final int ane;

    public MacOSPowermetricsSensor() {
        ane = AppleSiliconMeasure.METADATA.indexFor(AppleSiliconMeasure.ANE);
        cpu = AppleSiliconMeasure.METADATA.indexFor(AppleSiliconMeasure.CPU);
        gpu = AppleSiliconMeasure.METADATA.indexFor(AppleSiliconMeasure.GPU);
    }

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

    @Override
    public void update(OngoingPowerMeasure ongoingMeasure) {
        extractPowerMeasure(ongoingMeasure, powermetrics.getInputStream(), pid, false);
    }

    AppleSiliconMeasure extractPowerMeasure(InputStream powerMeasureInput, long pid) {
        return extractPowerMeasure(new OngoingPowerMeasure(AppleSiliconMeasure.METADATA), powerMeasureInput, " " + pid + " ",
                true);
    }

    AppleSiliconMeasure extractPowerMeasure(OngoingPowerMeasure ongoingMeasure,
            InputStream powerMeasureInput,
            String paddedPIDAsString, boolean returnCurrent) {
        try {
            // Should not be closed since it closes the process
            BufferedReader input = new BufferedReader(new InputStreamReader(powerMeasureInput));
            String line;
            double cpuShare = -1, gpuShare = -1;
            boolean totalDone = false;
            boolean cpuDone = false;
            // start measure
            ongoingMeasure.startNewMeasure();
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
                        final var jmxCpuShare = PowerMeasurer.instance().cpuShareOfJVMProcess();
                        ongoingMeasure.setComponent(cpu, extractAttributedMeasure(line, cpuShare));
                        accumulatedCPUShareDiff += (cpuShare - jmxCpuShare);
                        cpuDone = true;
                    }
                    continue;
                }

                if (line.startsWith("GPU Power")) {
                    ongoingMeasure.setComponent(gpu, extractAttributedMeasure(line, gpuShare));
                    continue;
                }

                if (line.startsWith("ANE Power")) {
                    ongoingMeasure.setComponent(ane, extractAttributedMeasure(line, 1));
                    break;
                }
            }

            final var measure = ongoingMeasure.stopMeasure();
            return returnCurrent ? new AppleSiliconMeasure(measure) : null;
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
    public OngoingPowerMeasure start(long duration, long frequency) throws Exception {
        final var freq = Long.toString(Math.round(frequency));
        powermetrics = Runtime.getRuntime()
                .exec("sudo powermetrics --samplers cpu_power,tasks --show-process-samp-norm --show-process-gpu -i "
                        + freq);
        accumulatedCPUShareDiff = 0.0;
        return new OngoingPowerMeasure(AppleSiliconMeasure.METADATA);
    }

    @Override
    public void stop() {
        powermetrics.destroy();
    }

    @Override
    public Optional<String> additionalInfo(PowerMeasure measure) {
        return Optional.of("Powermetrics vs JMX CPU share accumulated difference: " + accumulatedCPUShareDiff);
    }

    @Override
    public AppleSiliconMeasure measureFor(double[] measureComponents) {
        return new AppleSiliconMeasure(measureComponents);
    }
}
