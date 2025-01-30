package net.laprun.sustainability.power.quarkus.runtime;

import java.net.ConnectException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.sse.InboundSseEvent;
import jakarta.ws.rs.sse.SseEventSource;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.quarkus.logging.Log;
import io.quarkus.rest.client.reactive.jackson.runtime.serialisers.ClientJacksonMessageBodyReader;
import io.vertx.core.http.HttpClosedException;
import net.laprun.sustainability.power.SensorMeasure;
import net.laprun.sustainability.power.SensorMetadata;
import net.laprun.sustainability.power.SensorUnit;
import net.laprun.sustainability.power.measure.OngoingPowerMeasure;

public class ServerSampler {
    private static final long pid = ProcessHandle.current().pid();
    private final SseEventSource powerAPI;
    private final WebTarget base;
    private OngoingPowerMeasure measure;
    private SensorMetadata metadata;
    private TotalRecorder totalComp;
    private Consumer<DisplayableMeasure> completed;
    private Consumer<Throwable> errorHandler;
    private String status = "initialized";

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

    public synchronized Optional<SensorMetadata> metadata() {
            if (metadata == null) {
                try {
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
                        totalComp = new TotalRecorder(metadata, SensorUnit.mW, totalIndices);
                    }
                    status = "connected";
                } catch (Exception e) {
                    Log.warn("Failed to load sensor metadata", e);
                    status = "error: failed to load sensor metadata (" + e.getMessage() + ")";
                }
            }
            return Optional.ofNullable(metadata);
    }

    URI powerServerURI() {
        return powerServerURI;
    }

    /**
     * Only returns local synthetic components, if any
     */
    List<SensorMetadata.ComponentMetadata> localMetadata() {
        if (metadata == null) {
            return List.of();
        }
        return Optional.ofNullable(measure).map(m -> {
            // remove server metadata entries
            return m.metadata().components().entrySet().stream()
                    .filter((e) -> !metadata.exists(e.getKey()))
                    .map(Map.Entry::getValue)
                    .toList();

        }).orElse(List.of());
    }

    public String status() {
        return status;
    }

    public synchronized boolean isRunning() {
        return measure != null;
    }

    public OngoingPowerMeasure start(long durationInSeconds, long frequencyInMilliseconds) {
        try {
            synchronized (this) {
                final var metadata = metadata().orElseThrow(IllegalStateException::new);
                totalComp.reset(); // reset recorded values since we're recording a new measure
                measure = new OngoingPowerMeasure(metadata, totalComp);
                status = "started";
            }
            powerAPI.open();
            return measure;
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
            return null;
        }
    }

    public void stopOnError(Throwable e) {
        // ignore HttpClosedException todo: figure out why this exception occurs in the first place!
        if (!(e instanceof HttpClosedException) && errorHandler != null) {
            errorHandler.accept(e);
        }
        synchronized (this) {
            status = "error: measure failed (" + e.getMessage() + ")";
            if (measure != null && measure.numberOfSamples() > 0) {
                stop();
            }
        }
    }

    private void onComplete() {
        System.out.println("Power measurement finished!");
    }

    private void onEvent(InboundSseEvent event) {
        final var measureFromServer = event.readData(SensorMeasure.class);
        record(measureFromServer);
    }

    private void record(SensorMeasure measureFromServer) {
        if (measureFromServer != null) {
            final var components = measureFromServer.components();
            if (Arrays.equals(SensorMeasure.missing.components(), components)) {
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

    public DisplayableMeasure stop() {
        powerAPI.close();
        final DescriptiveStatistics stats;
        final DisplayableMeasure measured;
        synchronized (this) {
            stats = totalComp.statistics();
            measure = null;
            status = "stopped";
        }
        measured = new DisplayableMeasure(stats.getSum(), stats.getMin(), stats.getMax(), stats.getMean(), stats.getStandardDeviation(), stats.getValues());
        if (completed != null) {
            completed.accept(measured);
        }
        return measured;
    }

    public ServerSampler withCompletedHandler(Consumer<DisplayableMeasure> completed) {
        this.completed = completed;
        return this;
    }


    public ServerSampler withErrorHandler(Consumer<Throwable> errorHandler) {
        this.errorHandler = errorHandler;
        return this;
    }
}
