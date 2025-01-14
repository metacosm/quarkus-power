package net.laprun.sustainability.power.quarkus.deployment;

import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import net.laprun.sustainability.power.quarkus.runtime.PowerMeasurer;

class PowerProcessor {

    private static final String FEATURE = "power";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    PowerMeasurerBuildItem powerMeasurer() {
        return new PowerMeasurerBuildItem(PowerMeasurer.instance());
    }
}
