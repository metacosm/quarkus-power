package net.laprun.sustainability.power.quarkus.runtime;

import java.net.ConnectException;
import java.net.URI;
import java.util.Arrays;
import java.util.function.Consumer;

import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.sse.InboundSseEvent;
import jakarta.ws.rs.sse.SseEventSource;

import net.laprun.sustainability.power.SensorUnit;
import net.laprun.sustainability.power.analysis.TotalMeasureProcessor;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.quarkus.rest.client.reactive.jackson.runtime.serialisers.ClientJacksonMessageBodyReader;
import io.vertx.core.http.HttpClosedException;
import net.laprun.sustainability.power.SensorMeasure;
import net.laprun.sustainability.power.SensorMetadata;
import net.laprun.sustainability.power.measure.OngoingPowerMeasure;
import net.laprun.sustainability.power.measure.PowerMeasure;
import net.laprun.sustainability.power.measure.StoppedPowerMeasure;

public class ServerSampler implements Sampler {
    private final SseEventSource powerAPI;
    private final WebTarget base;
    private OngoingPowerMeasure measure;
    private SensorMetadata metadata;
    private TotalMeasureProcessor totalProc;
    private static final long pid = ProcessHandle.current().pid();
    private Consumer<PowerMeasure> completed;
    private Consumer<Throwable> errorHandler;

    @ConfigProperty(name = "power-server.url", defaultValue = "http://localhost:20432")
    URI powerServerURI;

    private static final URI DEFAULT_URI = URI.create("http://localhost:20432");

    public ServerSampler(URI powerServerURI) {
        this.powerServerURI = powerServerURI != null ? powerServerURI : DEFAULT_URI;
        final var client = ClientBuilder.newClient();
        client.register(new ClientJacksonMessageBodyReader(new ObjectMapper()));
        base = client.target(this.powerServerURI.resolve("power"));

        final var powerForPid = base.path("{pid}").resolveTemplate("pid", pid);
        powerAPI = SseEventSource.target(powerForPid).build();
        powerAPI.register(this::onEvent, this::stopOnError, this::onComplete);
    }

    public ServerSampler() {
        this(null);
    }

    @Override
    public SensorMetadata metadata() {
        synchronized (this) {
            if (metadata == null) {
                this.metadata = base.path("metadata").request(MediaType.APPLICATION_JSON_TYPE).get(SensorMetadata.class);

                // default total aggregation: all known components that W (or similar) as unit
                final var integerIndices = metadata.components().values().stream()
                        .filter(cm -> SensorUnit.W.isCommensurableWith(cm.unit()))
                        .map(SensorMetadata.ComponentMetadata::index)
                        .toArray(Integer[]::new);
                if (integerIndices.length > 0) {
                    final int[] totalIndices = new int[integerIndices.length];
                    for (int i = 0; i < totalIndices.length; i++) {
                        totalIndices[i] = integerIndices[i];
                    }
                    totalProc = new TotalMeasureProcessor(metadata, SensorUnit.mW, totalIndices);
                }
            }
            return metadata;
        }
    }

    @Override
    public String info() {
        return "Connected to " + powerServerURI + " (status: " + (isRunning() ? "running" : "stopped") + ")";
    }

    @Override
    public synchronized boolean isRunning() {
        return measure != null;
    }

    public void start(long durationInSeconds, long frequencyInMilliseconds) {
        try {
            synchronized (this) {
                final var metadata = metadata();
                measure = new OngoingPowerMeasure(metadata);
                if(totalProc != null) {
                    measure.registerMeasureProcessor(totalProc);
                }
            }
            powerAPI.open();
        } catch (Exception e) {
            if (e instanceof ProcessingException processingException) {
                final var cause = processingException.getCause();
                if (cause instanceof ConnectException connectException) {
                    stopOnError(new RuntimeException(
                            "Couldn't connect to power-server. Please see the instructions to set it up and run it.",
                            connectException));
                }
            }
            stopOnError(e);
        }
    }

    @Override
    public void stopOnError(Throwable e) {
        // ignore HttpClosedException todo: figure out why this exception occurs in the first place!
        if (!(e instanceof HttpClosedException)) {
            errorHandler.accept(e);
        }
        synchronized (this) {
            if (measure != null && measure.numberOfSamples() > 0) {
                stop();
            }
        }
    }

    private void onComplete() {
        System.out.println("Finished!");
    }

    private void onEvent(InboundSseEvent event) {
        final var measureFromServer = event.readData(SensorMeasure.class);
        record(measureFromServer);
    }

    private void record(SensorMeasure measureFromServer) {
        if (measureFromServer != null) {
            final var components = measureFromServer.components();
            if (Arrays.equals(new double[] { -1.0 }, components)) {
                System.out.println("Skipping invalid measure");
            } else {
                synchronized (this) {
                    if (measure != null) {
                        measure.recordMeasure(components);
                    } else {
                        System.out.println("No ongoing measure! Skipping for tick " + measureFromServer.tick());
                    }
                }
            }
        }
    }

    public synchronized void stop() {
        powerAPI.close();
        final var measured = new StoppedPowerMeasure(measure);
        measure = null;
        completed.accept(measured);
    }

    @Override
    public Sampler withCompletedHandler(Consumer<PowerMeasure> completed) {
        this.completed = completed;
        return this;
    }

    @Override
    public Sampler withErrorHandler(Consumer<Throwable> errorHandler) {
        this.errorHandler = errorHandler;
        return this;
    }
}
