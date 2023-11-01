package io.quarkiverse.power.runtime.sensors.macos.jmx;

import static io.quarkiverse.power.runtime.PowerMeasurer.osBean;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import io.quarkiverse.power.runtime.sensors.OngoingPowerMeasure;
import io.quarkiverse.power.runtime.sensors.PowerSensor;
import io.quarkiverse.power.runtime.sensors.macos.AppleSiliconMeasure;

@SuppressWarnings("unused")
public class JMXCPUSensor implements PowerSensor<AppleSiliconMeasure> {
    public static PowerSensor<AppleSiliconMeasure> instance = new JMXCPUSensor();
    private Process powermetrics;

    @Override
    public OngoingPowerMeasure<AppleSiliconMeasure> start(long duration, long frequency, Writer out)
            throws Exception {
        final var freq = Long.toString(Math.round(frequency));
        powermetrics = Runtime.getRuntime().exec("sudo powermetrics --samplers cpu_power -i " + freq);
        return new OngoingPowerMeasure<>(new AppleSiliconMeasure());
    }

    public void update(OngoingPowerMeasure<AppleSiliconMeasure> ongoingMeasure, Writer out) {
        try {
            // Should not be closed since it closes the process
            BufferedReader input = new BufferedReader(new InputStreamReader(powermetrics.getInputStream()));
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
                    ongoingMeasure.addCPU(extractAttributedMeasure(line, cpuShare));
                    break;
                }
            }
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
    public void stop() {
        powermetrics.destroy();
    }
}
