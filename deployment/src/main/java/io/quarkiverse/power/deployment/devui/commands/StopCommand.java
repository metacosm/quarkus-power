package io.quarkiverse.power.deployment.devui.commands;

import org.aesh.command.CommandDefinition;
import org.aesh.command.CommandException;
import org.aesh.command.CommandResult;
import org.aesh.command.invocation.CommandInvocation;

import io.quarkiverse.power.runtime.PowerMeasurer;
import io.quarkiverse.power.runtime.SensorMeasure;
import io.quarkus.deployment.console.QuarkusCommand;

@CommandDefinition(name = "stop", description = "Stops power measurement and outputs accumulated power since measures were started")
public class StopCommand extends QuarkusCommand {

    private final PowerMeasurer<? extends SensorMeasure> sensor;

    public StopCommand(PowerMeasurer<? extends SensorMeasure> sensor) {
        this.sensor = sensor;
    }

    @Override
    public CommandResult doExecute(CommandInvocation commandInvocation) throws CommandException, InterruptedException {
        sensor.stop(commandInvocation::println);

        return CommandResult.SUCCESS;
    }
}
