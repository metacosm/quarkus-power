package net.laprun.sustainability.power.quarkus.deployment.devui;

import io.quarkus.deployment.IsDevelopment;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.BuildSteps;
import io.quarkus.deployment.builditem.ConsoleCommandBuildItem;
import io.quarkus.devui.spi.JsonRPCProvidersBuildItem;
import io.quarkus.devui.spi.page.CardPageBuildItem;
import io.quarkus.devui.spi.page.Page;
import net.laprun.sustainability.power.quarkus.deployment.devui.commands.PowerCommands;
import net.laprun.sustainability.power.quarkus.runtime.devui.PowerService;

@BuildSteps(onlyIf = IsDevelopment.class)
public class DevUI {

    @BuildStep
    void addConsoleCommands(BuildProducer<ConsoleCommandBuildItem> commands) {
        // register dev console commands
        commands.produce(new ConsoleCommandBuildItem(new PowerCommands()));
    }

    @BuildStep
    public CardPageBuildItem pages() {
        CardPageBuildItem card = new CardPageBuildItem();
        card.addPage(Page.webComponentPageBuilder()
                .icon("font-awesome-solid:info")
                .componentLink("qwc-power-info.js"));
        return card;
    }

    @BuildStep
    JsonRPCProvidersBuildItem powerMeasurerJSONProvider() {
        return new JsonRPCProvidersBuildItem(PowerService.class);
    }
}
