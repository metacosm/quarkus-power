package net.laprun.sustainability.power.quarkus.deployment.devui.commands;

import io.quarkus.deployment.console.QuarkusCommand;
import net.laprun.sustainability.power.quarkus.runtime.PowerMeasure;
import net.laprun.sustainability.power.quarkus.runtime.PowerMeasurer;
import org.aesh.command.CommandDefinition;
import org.aesh.command.CommandResult;
import org.aesh.command.invocation.CommandInvocation;
import org.aesh.command.option.Option;

@CommandDefinition(name = "start", description = "Starts measuring power consumption of the current application")
public class StartCommand extends QuarkusCommand {
    private final PowerMeasurer sensor;
    private PowerMeasure baseline;

    @Option(name = "stopAfter", shortName = 's', description = "Automatically stop the measures after the specified duration in seconds", defaultValue = "-1")
    private long duration;

    @Option(name = "frequency", shortName = 'f', description = "The frequency at which measurements should be taken, in milliseconds", defaultValue = "1000")
    private long frequency;

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
                    commandInvocation.println("Establishing baseline for 30s, please do not use your application until done.");
                    commandInvocation.println("Power measurement will start as configured after this initial measure is done.");
                    sensor.start(30, 1000);
                    sensor.onError(e -> commandInvocation.println("An error occurred: " + e.getMessage()));
                    sensor.onCompleted((m) -> {
                        baseline = m;
                        outputConsumptionSinceStarted(baseline, commandInvocation, true);
                        commandInvocation.println("Baseline established! You can now interact with your application normally.");

                        try {
                            sensor.start(duration, frequency);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                        sensor.onCompleted(
                                (finished) -> outputConsumptionSinceStarted(finished, commandInvocation, false));
                    });
                } else {
                    sensor.start(duration, frequency);
                    sensor.onCompleted((m) -> outputConsumptionSinceStarted(m, commandInvocation, false));
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

    private void outputConsumptionSinceStarted(PowerMeasure measure, CommandInvocation out, boolean isBaseline) {
        final var title = isBaseline ? "\nBaseline => " : "\nMeasured => ";
        out.println(title + PowerMeasure.asString(measure));
        if (!isBaseline) {
            sensor.additionalSensorInfo().ifPresent(out::println);
            out.println("Baseline => " + PowerMeasure.asString(baseline));
            out.println("Average ∆ => " + PowerMeasure.readableWithUnit(measure.average() - baseline.average()));
        }
    }
}
