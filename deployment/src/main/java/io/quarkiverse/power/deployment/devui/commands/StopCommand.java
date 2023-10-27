package io.quarkiverse.power.deployment.devui.commands;

import org.aesh.command.CommandDefinition;
import org.aesh.command.CommandException;
import org.aesh.command.CommandResult;
import org.aesh.command.invocation.CommandInvocation;

import io.quarkiverse.power.runtime.PowerSensor;
import io.quarkiverse.power.runtime.sensors.macos.powermetrics.MacOSPowermetricsSensor;
import io.quarkus.deployment.console.QuarkusCommand;

@CommandDefinition(name = "stop", description = "Stops power measurement and outputs accumulated power since measures were started")
@SuppressWarnings("rawtypes")
public class StopCommand extends QuarkusCommand {

    //    @Inject
    PowerSensor sensor = MacOSPowermetricsSensor.instance;

    @Override
    public CommandResult doExecute(CommandInvocation commandInvocation) throws CommandException, InterruptedException {
        sensor.stop();
        sensor.outputConsumptionSinceStarted(commandInvocation::println);

        return CommandResult.SUCCESS;
    }
}
