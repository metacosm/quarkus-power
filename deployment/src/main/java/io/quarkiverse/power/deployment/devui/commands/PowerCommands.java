package io.quarkiverse.power.deployment.devui.commands;

import java.util.List;

import org.aesh.command.*;
import org.aesh.command.invocation.CommandInvocation;

@GroupCommandDefinition(name = "power", description = "Power consumption commands", generateHelp = true)
@SuppressWarnings("rawtypes")
public class PowerCommands implements GroupCommand {

    public PowerCommands() {
    }

    @Override
    public List<Command> getCommands() {
        return List.of(new StartCommand(), new StopCommand());
    }

    @Override
    public CommandResult execute(CommandInvocation commandInvocation) throws CommandException, InterruptedException {
        commandInvocation.println(commandInvocation.getHelpInfo());
        return CommandResult.SUCCESS;
    }
}
