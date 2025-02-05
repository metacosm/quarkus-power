package net.laprun.sustainability.power.quarkus.runtime;

import net.laprun.sustainability.power.measure.Timing;

public record DisplayableMeasure(double total, double min, double max, double avg, double stdDev, double[] measures, Timing timestamps) {
    @Override
    public String toString() {
        return String.format("total: %s (min: %s / max: %s / avg: %s / σ: %s)", withUnit(total), withUnit(min), withUnit(max), withUnit(avg), withUnit(stdDev));
    }

    public String getSummary() {
        return toString();
    }

    public int getSamplesCount() {
        return measures.length;
    }

    public static String withUnit(double mWValue) {
        var unit = "mW";
        double value = mWValue;
        if (mWValue > 1000) {
            unit = "W";
            value = mWValue / 1000;
        }

        return String.format("%.2f%s", value, unit);
    }
}
