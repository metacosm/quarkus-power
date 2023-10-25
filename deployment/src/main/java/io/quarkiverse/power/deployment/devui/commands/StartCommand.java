package io.quarkiverse.power.deployment.devui.commands;

import org.aesh.command.Command;
import org.aesh.command.CommandDefinition;
import org.aesh.command.CommandResult;
import org.aesh.command.invocation.CommandInvocation;
import org.aesh.command.option.Option;

import io.quarkiverse.power.runtime.MacOSPowermetricsSensor;
import io.quarkiverse.power.runtime.PowerSensor;

@CommandDefinition(name = "start", description = "Starts measuring power consumption of the current application", generateHelp = true)
@SuppressWarnings("rawtypes")
public class StartCommand implements Command {

    //    @Inject
    PowerSensor sensor = MacOSPowermetricsSensor.instance;

    @Option(name = "duration", shortName = 'd', description = "The duration during which measurements should be taken before automatically stopping, in seconds", defaultValue = "-1")
    private long duration;

    @Option(name = "frequency", shortName = 'f', description = "The frequency at which measurements should be taken, in milliseconds", defaultValue = "1000")
    private long frequency;

    @Override
    public CommandResult execute(CommandInvocation commandInvocation) {
        try {
            if (duration > 0) {
                commandInvocation.println("Measuring power for " + duration + " seconds, every " + frequency + " milliseconds");
            } else {
                commandInvocation.println("Measuring power every " + frequency
                        + " milliseconds. Execute 'power stop' to stop measurements and get the results.");
            }
            sensor.start(duration, frequency, commandInvocation::println);
        } catch (Exception e) {
            commandInvocation.println("Couldn't start power measure: " + e.getMessage());
            return CommandResult.FAILURE;
        }
        return CommandResult.SUCCESS;
    }
}
