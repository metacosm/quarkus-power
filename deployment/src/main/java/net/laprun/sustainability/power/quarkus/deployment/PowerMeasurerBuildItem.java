package net.laprun.sustainability.power.quarkus.deployment;

import io.quarkus.builder.item.SimpleBuildItem;
import net.laprun.sustainability.power.quarkus.runtime.PowerMeasurer;

public final class PowerMeasurerBuildItem extends SimpleBuildItem {
    private final PowerMeasurer measurer;

    public PowerMeasurerBuildItem(PowerMeasurer measurer) {
        this.measurer = measurer;
    }

    public PowerMeasurer getMeasurer() {
        return measurer;
    }
}
