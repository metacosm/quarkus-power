package net.laprun.sustainability.power.quarkus.deployment.devui.commands;

import org.aesh.command.CommandDefinition;
import org.aesh.command.CommandResult;
import org.aesh.command.invocation.CommandInvocation;
import org.aesh.command.option.Option;

import io.quarkus.deployment.console.QuarkusCommand;
import net.laprun.sustainability.power.measure.PowerMeasure;
import net.laprun.sustainability.power.quarkus.runtime.PowerMeasurer;

@CommandDefinition(name = "start", description = "Starts measuring power consumption of the current application")
public class StartCommand extends QuarkusCommand {
    private final PowerMeasurer sensor;
    private PowerMeasure baseline;

    @Option(name = "stopAfter", shortName = 's', description = "Automatically stop the measures after the specified duration in seconds", defaultValue = "-1")
    private long duration;

    @Option(name = "frequency", shortName = 'f', description = "The frequency at which measurements should be taken, in milliseconds", defaultValue = "1000")
    private long frequency;

    @Option(name = "baselineDuration", shortName = 'b', description = "Duration during which a baseline will be established, in seconds", defaultValue = "15")
    private int baselineDuration;

    public StartCommand(PowerMeasurer sensor) {
        this.sensor = sensor;
    }

    @Override
    public CommandResult doExecute(CommandInvocation commandInvocation) {
        try {
            if (!sensor.isRunning()) {
                if (duration > 0) {
                    commandInvocation
                            .println("Measuring power for " + duration + " seconds, every " + frequency + " milliseconds");
                } else {
                    commandInvocation.println("Measuring power every " + frequency
                            + " milliseconds. Execute 'power stop' to stop measurements and get the results.");
                }

                if (baseline == null) {
                    commandInvocation.println("Establishing baseline for " + baselineDuration
                            + "s, please do not use your application until done.");
                    commandInvocation.println("Power measurement will start as configured after this initial measure is done.");
                    sensor.withCompletedHandler((m) -> establishBaseline(commandInvocation, m))
                            .withErrorHandler(e -> handleError(commandInvocation, e))
                            .start(baselineDuration, 1000);
                } else {
                    sensor.withCompletedHandler((m) -> outputConsumptionSinceStarted(m, commandInvocation, false))
                            .start(duration, frequency);
                }

            } else {
                commandInvocation.println("Power measurement is already ongoing. Execute 'power stop' to stop it now.");
            }
        } catch (Exception e) {
            commandInvocation.println("Couldn't start power measure: " + e.getMessage());
            return CommandResult.FAILURE;
        }
        return CommandResult.SUCCESS;
    }

    private void handleError(CommandInvocation commandInvocation, Throwable e) {
        commandInvocation.println("An error occurred: " + e.getMessage());
    }

    private void establishBaseline(CommandInvocation commandInvocation, PowerMeasure m) {
        baseline = m;
        outputConsumptionSinceStarted(baseline, commandInvocation, true);
        commandInvocation
                .println("Baseline established! You can now interact with your application normally.");
        try {
            sensor.start(duration, frequency);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        sensor.withCompletedHandler(
                (finished) -> outputConsumptionSinceStarted(finished, commandInvocation, false));
    }

    private void outputConsumptionSinceStarted(PowerMeasure measure, CommandInvocation out, boolean isBaseline) {
        final var title = isBaseline ? "\nBaseline => " : "\nMeasured => ";
        out.println(title + PowerMeasure.asString(measure));
        if (!isBaseline) {
            out.println("Baseline => " + PowerMeasure.asString(baseline));
            out.println("Average ∆ => " + PowerMeasure.readableWithUnit(measure.average() - baseline.average()));
        }
    }
}
