package io.quarkiverse.power.deployment.devui.commands;

import io.quarkiverse.power.runtime.PowerMeasurer;
import org.aesh.command.Command;
import org.aesh.command.CommandException;
import org.aesh.command.CommandResult;
import org.aesh.command.GroupCommand;
import org.aesh.command.GroupCommandDefinition;
import org.aesh.command.invocation.CommandInvocation;

import java.util.List;

@GroupCommandDefinition(name = "power", description = "Power consumption commands", generateHelp = true)
@SuppressWarnings("rawtypes")
public class PowerCommands implements GroupCommand {
    private final PowerMeasurer sensor;

    public PowerCommands(PowerMeasurer sensor) {
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
