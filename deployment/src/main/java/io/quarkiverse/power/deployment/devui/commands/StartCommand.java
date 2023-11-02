package io.quarkiverse.power.deployment.devui.commands;

import org.aesh.command.CommandDefinition;
import org.aesh.command.CommandException;
import org.aesh.command.CommandResult;
import org.aesh.command.invocation.CommandInvocation;
import org.aesh.command.option.Option;

import io.quarkiverse.power.runtime.PowerMeasurer;
import io.quarkiverse.power.runtime.SensorMeasure;
import io.quarkus.deployment.console.QuarkusCommand;

@CommandDefinition(name = "start", description = "Starts measuring power consumption of the current application")
public class StartCommand extends QuarkusCommand {
    private final PowerMeasurer<? extends SensorMeasure> sensor;

    @Option(name = "stopAfter", shortName = 's', description = "Automatically stop the measures after the specified duration in seconds", defaultValue = "-1")
    private long duration;

    @Option(name = "frequency", shortName = 'f', description = "The frequency at which measurements should be taken, in milliseconds", defaultValue = "1000")
    private long frequency;

    public StartCommand(PowerMeasurer<? extends SensorMeasure> sensor) {
        this.sensor = sensor;
    }

    @Override
    public CommandResult doExecute(CommandInvocation commandInvocation) throws CommandException, InterruptedException {
        try {
            if (!sensor.isRunning()) {
                if (duration > 0) {
                    commandInvocation
                            .println("Measuring power for " + duration + " seconds, every " + frequency + " milliseconds");
                } else {
                    commandInvocation.println("Measuring power every " + frequency
                            + " milliseconds. Execute 'power stop' to stop measurements and get the results.");
                }
                sensor.start(duration, frequency, commandInvocation::println);
            } else {
                commandInvocation.println("Power measurement is already ongoing. Execute 'power stop' to stop it now.");
            }
        } catch (Exception e) {
            commandInvocation.println("Couldn't start power measure: " + e.getMessage());
            return CommandResult.FAILURE;
        }
        return CommandResult.SUCCESS;
    }
}
