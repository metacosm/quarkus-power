package net.laprun.sustainability.power.quarkus.deployment;

import io.quarkus.deployment.IsDevelopment;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.ConsoleCommandBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import net.laprun.sustainability.power.quarkus.deployment.devui.commands.PowerCommands;
import net.laprun.sustainability.power.quarkus.runtime.PowerMeasurer;

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
