package net.laprun.sustainability.power.quarkus.deployment.devui.commands;

import org.aesh.command.CommandDefinition;
import org.aesh.command.CommandResult;
import org.aesh.command.invocation.CommandInvocation;

import io.quarkus.deployment.console.QuarkusCommand;
import net.laprun.sustainability.power.quarkus.runtime.PowerMeasurer;

@CommandDefinition(name = "info", description = "Provides information about power measurement, in particular available sensors")
public class InfoCommand extends QuarkusCommand {
    private final PowerMeasurer powerMeasurer;

    public InfoCommand(PowerMeasurer powerMeasurer) {
        this.powerMeasurer = powerMeasurer;
    }

    @Override
    public CommandResult doExecute(CommandInvocation commandInvocation) {
        commandInvocation.println(powerMeasurer.sampler().info());
        commandInvocation.println("\n====\nSensor metadata:\n" + powerMeasurer.metadata());
        return CommandResult.SUCCESS;
    }
}
