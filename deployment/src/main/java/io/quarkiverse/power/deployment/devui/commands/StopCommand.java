package io.quarkiverse.power.deployment.devui.commands;

import org.aesh.command.Command;
import org.aesh.command.CommandDefinition;
import org.aesh.command.CommandResult;
import org.aesh.command.invocation.CommandInvocation;

import io.quarkiverse.power.runtime.MacOSPowermetricsSensor;
import io.quarkiverse.power.runtime.PowerSensor;

@CommandDefinition(name = "stop", description = "Stops power measurement and outputs accumulated power since measures were started")
@SuppressWarnings("rawtypes")
public class StopCommand implements Command {

    //    @Inject
    PowerSensor sensor = MacOSPowermetricsSensor.instance;

    @Override
    public CommandResult execute(CommandInvocation commandInvocation) {
        sensor.stop();
        sensor.outputConsumptionSinceStarted(commandInvocation::println);

        return CommandResult.SUCCESS;
    }
}
