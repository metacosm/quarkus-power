package io.quarkiverse.power.runtime;

import java.util.Arrays;
import java.util.function.Consumer;

import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.sse.SseEventSource;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.metacosm.power.SensorMetadata;
import io.quarkiverse.power.runtime.sensors.OngoingPowerMeasure;
import io.quarkiverse.power.runtime.sensors.StoppedPowerMeasure;
import io.quarkus.rest.client.reactive.jackson.runtime.serialisers.ClientJacksonMessageBodyReader;

public class ServerSampler implements Sampler {
    private final SseEventSource powerAPI;
    private final WebTarget base;
    private OngoingPowerMeasure measure;
    private io.quarkiverse.power.runtime.SensorMetadata metadata;
    private static final long pid = ProcessHandle.current().pid();

    public ServerSampler() {
        final var client = ClientBuilder.newClient();
        client.register(new ClientJacksonMessageBodyReader(new ObjectMapper()));
        base = client.target("http://localhost:20432/power");

        final var powerForPid = base.path("{pid}").resolveTemplate("pid", pid);
        powerAPI = SseEventSource.target(powerForPid).build();
        powerAPI.register((sseEvent) -> update(sseEvent.readData()), (e) -> System.out.println("Exception: " + e.getMessage()));
    }

    @Override
    public boolean isRunning() {
        return measure != null;
    }

    public void start(long durationInSeconds, long frequencyInMilliseconds) throws Exception {
        if (metadata == null) {
            final var serverMetadata = base.path("metadata").request(MediaType.APPLICATION_JSON_TYPE).get(SensorMetadata.class);
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
    }

    private void update(String measureAsString) {
        if (measureAsString != null) {
            final var components = Arrays.stream(measureAsString.split(" ")).mapToDouble(Double::parseDouble).toArray();
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
