package net.laprun.sustainability.power.quarkus.deployment.devui.commands;

import io.quarkus.deployment.console.QuarkusCommand;
import net.laprun.sustainability.power.quarkus.runtime.PowerMeasurer;
import org.aesh.command.CommandDefinition;
import org.aesh.command.CommandResult;
import org.aesh.command.invocation.CommandInvocation;

@CommandDefinition(name = "stop", description = "Stops power measurement and outputs accumulated power since measures were started")
public class StopCommand extends QuarkusCommand {

    private final PowerMeasurer sensor;

    public StopCommand(PowerMeasurer sensor) {
        this.sensor = sensor;
    }

    @Override
    public CommandResult doExecute(CommandInvocation commandInvocation) {
        sensor.onError(e -> commandInvocation.println("An error occurred: " + e.getMessage()));
        if (sensor.isRunning()) {
            sensor.stop();
        } else {
            commandInvocation.println("Power measurement hasn't started. Execute 'power start' to start it first.");
        }

        return CommandResult.SUCCESS;
    }
}
