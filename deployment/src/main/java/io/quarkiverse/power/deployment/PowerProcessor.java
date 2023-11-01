package io.quarkiverse.power.deployment;

import io.quarkiverse.power.deployment.devui.commands.PowerCommands;
import io.quarkiverse.power.runtime.PowerMeasurer;
import io.quarkus.deployment.IsDevelopment;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.ConsoleCommandBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;

class PowerProcessor {

    private static final String FEATURE = "power";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep(onlyIf = IsDevelopment.class)
    void addConsoleCommands(BuildProducer<ConsoleCommandBuildItem> commands) {
        // register dev console commands
        commands.produce(new ConsoleCommandBuildItem(new PowerCommands(PowerMeasurer.instance())));
    }
}
