package io.quarkiverse.power.deployment.devui.commands;

import java.util.List;

import org.aesh.command.*;
import org.aesh.command.invocation.CommandInvocation;

import io.quarkiverse.power.runtime.PowerMeasurer;
import io.quarkiverse.power.runtime.SensorMeasure;

@GroupCommandDefinition(name = "power", description = "Power consumption commands", generateHelp = true)
@SuppressWarnings("rawtypes")
public class PowerCommands implements GroupCommand {
    private final PowerMeasurer<? extends SensorMeasure> sensor;

    public PowerCommands(PowerMeasurer<? extends SensorMeasure> sensor) {
        this.sensor = sensor;
    }

    @Override
    public List<Command> getCommands() {
        return List.of(new StartCommand(sensor), new StopCommand(sensor));
    }

    @Override
    public CommandResult execute(CommandInvocation commandInvocation) throws CommandException, InterruptedException {
        commandInvocation.println(commandInvocation.getHelpInfo());
        return CommandResult.SUCCESS;
    }
}
