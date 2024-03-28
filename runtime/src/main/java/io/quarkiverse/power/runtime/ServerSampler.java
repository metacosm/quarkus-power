package io.quarkiverse.power.runtime;

import java.net.ConnectException;
import java.net.URI;
import java.util.Arrays;
import java.util.function.Consumer;

import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.sse.SseEventSource;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.quarkus.rest.client.reactive.jackson.runtime.serialisers.ClientJacksonMessageBodyReader;
import net.laprun.sustainability.power.SensorMeasure;
import net.laprun.sustainability.power.SensorMetadata;

public class ServerSampler implements Sampler {
    private final SseEventSource powerAPI;
    private final WebTarget base;
    private OngoingPowerMeasure measure;
    private io.quarkiverse.power.runtime.SensorMetadata metadata;
    private static final long pid = ProcessHandle.current().pid();

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
        powerAPI.register((sseEvent) -> update(sseEvent.readData(SensorMeasure.class)),
                (e) -> System.out.println("Exception: " + e.getMessage()));
    }

    public ServerSampler() {
        this(null);
    }

    @Override
    public boolean isRunning() {
        return measure != null;
    }

    public void start(long durationInSeconds, long frequencyInMilliseconds) throws Exception {
        try {
            if (metadata == null) {
                final var serverMetadata = base.path("metadata").request(MediaType.APPLICATION_JSON_TYPE)
                        .get(SensorMetadata.class);
                this.metadata = new io.quarkiverse.power.runtime.SensorMetadata() {
                    @Override
                    public int indexFor(String component) {
                        return serverMetadata.metadataFor(component).index();
                    }

                    @Override
                    public int componentCardinality() {
                        return serverMetadata.componentCardinality();
                    }
                };
            }

            measure = new OngoingPowerMeasure(metadata, durationInSeconds, frequencyInMilliseconds);
            powerAPI.open();
        } catch (Exception e) {
            if (e instanceof ProcessingException processingException) {
                final var cause = processingException.getCause();
                if (cause instanceof ConnectException connectException) {
                    throw new RuntimeException(
                            "Couldn't connect to power-server. Please see the instructions to set it up and run it.",
                            connectException);
                }
            }
            throw new RuntimeException(e);
        }
    }

    private void update(SensorMeasure measureFromServer) {
        if (measureFromServer != null) {
            final var components = measureFromServer.components();
            if (Arrays.equals(new double[] { -1.0 }, components)) {
                System.out.println("Skipping invalid measure");
            } else {
                measure.recordMeasure(components);
            }
        }
    }

    public void stop(Consumer<PowerMeasure> completed) {
        powerAPI.close();
        final var measured = new StoppedPowerMeasure(measure);
        measure = null;
        completed.accept(measured);
    }
}
