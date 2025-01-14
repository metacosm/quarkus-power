package net.laprun.sustainability.power.quarkus.deployment.devui;

import io.quarkus.deployment.IsDevelopment;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.BuildSteps;
import io.quarkus.deployment.builditem.ConsoleCommandBuildItem;
import net.laprun.sustainability.power.quarkus.deployment.PowerMeasurerBuildItem;
import net.laprun.sustainability.power.quarkus.deployment.devui.commands.PowerCommands;

@BuildSteps(onlyIf = IsDevelopment.class)
public class DevUI {

    @BuildStep
    void addConsoleCommands(PowerMeasurerBuildItem powerMeasurerBI, BuildProducer<ConsoleCommandBuildItem> commands) {
        // register dev console commands
        commands.produce(new ConsoleCommandBuildItem(new PowerCommands(powerMeasurerBI.getMeasurer())));
    }
}
