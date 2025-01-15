package net.laprun.sustainability.power.quarkus.deployment;

import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.arc.processor.DotNames;
import io.quarkus.deployment.annotations.BuildProducer;
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
    void powerMeasurer(BuildProducer<AdditionalBeanBuildItem> additionalBeans) {
        additionalBeans.produce(AdditionalBeanBuildItem.builder()
                .addBeanClass(PowerMeasurer.class)
                .setDefaultScope(DotNames.SINGLETON)
                .build());
    }
}
