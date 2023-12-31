package io.quarkiverse.power.runtime.sensors.macos.jmx;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import io.quarkiverse.power.runtime.PowerMeasurer;
import io.quarkiverse.power.runtime.sensors.OngoingPowerMeasure;
import io.quarkiverse.power.runtime.sensors.PowerSensor;
import io.quarkiverse.power.runtime.sensors.macos.AppleSiliconMeasure;

@SuppressWarnings("unused")
public class JMXCPUSensor implements PowerSensor<AppleSiliconMeasure> {
    public static PowerSensor<AppleSiliconMeasure> instance = new JMXCPUSensor();
    private Process powermetrics;
    private int cpu;

    @Override
    public OngoingPowerMeasure start(long duration, long frequency)
            throws Exception {
        final var freq = Long.toString(Math.round(frequency));
        powermetrics = Runtime.getRuntime().exec("sudo powermetrics --samplers cpu_power -i " + freq);
        cpu = AppleSiliconMeasure.METADATA.indexFor(AppleSiliconMeasure.CPU);
        return new OngoingPowerMeasure(AppleSiliconMeasure.METADATA, duration, frequency);
    }

    public void update(OngoingPowerMeasure ongoingMeasure) {
        try {
            // Should not be closed since it closes the process
            BufferedReader input = new BufferedReader(new InputStreamReader(powermetrics.getInputStream()));
            String line;
            ongoingMeasure.startNewMeasure();
            while ((line = input.readLine()) != null) {
                if (line.isEmpty() || line.startsWith("*")) {
                    continue;
                }

                // look for line that contains CPU power measure
                if (line.startsWith("CPU Power")) {
                    final var cpuShare = PowerMeasurer.instance().cpuShareOfJVMProcess();
                    if (cpuShare <= 0) {
                        break;
                    }
                    ongoingMeasure.setComponent(cpu, extractAttributedMeasure(line, cpuShare));
                    break;
                }
            }
            ongoingMeasure.stopMeasure();
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    @Override
    public AppleSiliconMeasure measureFor(double[] measureComponents) {
        return new AppleSiliconMeasure(measureComponents);
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
