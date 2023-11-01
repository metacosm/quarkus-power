package io.quarkiverse.power.runtime.sensors.macos.powermetrics;

import static io.quarkiverse.power.runtime.PowerMeasurer.osBean;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import io.quarkiverse.power.runtime.sensors.OngoingPowerMeasure;
import io.quarkiverse.power.runtime.sensors.PowerSensor;
import io.quarkiverse.power.runtime.sensors.macos.AppleSiliconMeasure;

public class MacOSPowermetricsSensor implements PowerSensor<AppleSiliconMeasure> {
    private Process powermetrics;
    public static PowerSensor<AppleSiliconMeasure> instance = new MacOSPowermetricsSensor();
    private final static String pid = " " + ProcessHandle.current().pid() + " ";
    private double accumulatedCPUShareDiff;

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
    public void update(OngoingPowerMeasure<AppleSiliconMeasure> ongoingMeasure, Writer out) {
        extractPowerMeasure(ongoingMeasure, powermetrics.getInputStream(), pid);
    }

    AppleSiliconMeasure extractPowerMeasure(InputStream powerMeasureInput, long pid) {
        return extractPowerMeasure(new OngoingPowerMeasure<>(new AppleSiliconMeasure()), powerMeasureInput, " " + pid + " ");
    }

    AppleSiliconMeasure extractPowerMeasure(OngoingPowerMeasure<AppleSiliconMeasure> ongoingMeasure,
            InputStream powerMeasureInput,
            String paddedPIDAsString) {
        final var accumulatedPower = ongoingMeasure.sensorMeasure();
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
                        final var processCpuLoad = osBean.getProcessCpuLoad();
                        final var cpuLoad = osBean.getCpuLoad();
                        final var jmxCpuShare = (processCpuLoad < 0 || cpuLoad <= 0) ? 0 : processCpuLoad / cpuLoad;
                        accumulatedPower.addCPU(extractAttributedMeasure(line, cpuShare));
                        accumulatedCPUShareDiff += (cpuShare - jmxCpuShare);
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
    public OngoingPowerMeasure<AppleSiliconMeasure> start(long duration, long frequency, final Writer out) throws Exception {
        final var freq = Long.toString(Math.round(frequency));
        powermetrics = Runtime.getRuntime()
                .exec("sudo powermetrics --samplers cpu_power,tasks --show-process-samp-norm --show-process-gpu -i "
                        + freq);
        return new OngoingPowerMeasure<>(new AppleSiliconMeasure());
    }

    @Override
    public void stop() {
        powermetrics.destroy();
    }

    public void additionalInfo(Writer out) {
        out.println("Powermetrics vs JMX CPU share accumulated difference: " + accumulatedCPUShareDiff);
    }
}
