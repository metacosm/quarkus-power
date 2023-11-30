package io.quarkiverse.power.runtime;

import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.sse.SseEventSource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.management.OperatingSystemMXBean;

import io.github.metacosm.power.SensorMetadata;
import io.quarkiverse.power.runtime.sensors.*;

public class PowerMeasurer<M extends SensorMeasure> {
    private static final OperatingSystemMXBean osBean;

    static {
        osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        // take two measures to avoid initial zero values
        osBean.getProcessCpuLoad();
        osBean.getCpuLoad();
        osBean.getProcessCpuLoad();
        osBean.getCpuLoad();
    }

    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> scheduled;
    private final PowerSensor<M> sensor;
    private OngoingPowerMeasure measure;

    private Consumer<PowerMeasure> completed;
    private BiConsumer<Integer, PowerMeasure> sampled;
    private Consumer<Exception> errorHandler;
    private SseEventSource powerAPI;

    private final static PowerMeasurer<? extends SensorMeasure> instance = new PowerMeasurer<>(
            PowerSensorProducer.determinePowerSensor());

    public static PowerMeasurer<? extends SensorMeasure> instance() {
        return instance;
    }

    public PowerMeasurer(PowerSensor<M> sensor) {
        this.sensor = sensor;
        this.onError(null);
    }

    public double cpuShareOfJVMProcess() {
        final var processCpuLoad = osBean.getProcessCpuLoad();
        final var cpuLoad = osBean.getCpuLoad();
        return (processCpuLoad < 0 || cpuLoad <= 0) ? 0 : processCpuLoad / cpuLoad;
    }

    public void onCompleted(Consumer<PowerMeasure> completed) {
        this.completed = completed;
    }

    public void onSampled(BiConsumer<Integer, PowerMeasure> sampled) {
        this.sampled = sampled;
    }

    public void onError(Consumer<Exception> errorHandler) {
        this.errorHandler = errorHandler != null ? errorHandler : (exception) -> {
            throw new RuntimeException(exception);
        };
    }

    public Optional<String> additionalSensorInfo() {
        return Optional.ofNullable(measure).flatMap(sensor::additionalInfo);
    }

    public boolean isRunning() {
        return measure != null;
    }

    public void start(long durationInSeconds, long frequencyInMilliseconds)
            throws Exception {
        try {
            measure = sensor.start(durationInSeconds, frequencyInMilliseconds);

            if (durationInSeconds > 0) {
                executor.schedule(this::stop, durationInSeconds, TimeUnit.SECONDS);
            }

            final var pid = ProcessHandle.current().pid();
            Client client = ClientBuilder.newClient();

            final var base = client.target("http://localhost:20432/power");
            ObjectMapper mapper = new ObjectMapper();

            final var response = base.path("metadata").request().get();
            //            final var response = base.path("metadata").request().get(SensorMetadata.class);  <- this doesn't work
            final var inputStream = response.readEntity(InputStream.class);
            final var metadata = mapper.readValue(inputStream, SensorMetadata.class);

            final var powerForPid = base.path("{pid}").resolveTemplate("pid", pid);

            powerAPI = SseEventSource.target(powerForPid).build();

            powerAPI.register((sseEvent) -> {
                System.out.println("Received: " + sseEvent.getName() + " - " + sseEvent.readData());

            }, (e) -> {
                System.out.println("Exception: " + e.getMessage());
            });
            powerAPI.open();

            scheduled = executor.scheduleAtFixedRate(this::update, 0, frequencyInMilliseconds, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            handleError(e);
        }
    }

    private void update() {
        try {
            sensor.update(measure);
            if (this.sampled != null) {
                sampled.accept(measure.numberOfSamples(), measure);
            }
        } catch (Exception e) {
            handleError(e);
        }
    }

    private void handleError(Exception e) {
        errorHandler.accept(e);
        try {
            if (scheduled != null) {
                scheduled.cancel(true);
            }
            if (sensor != null) {
                sensor.stop();
            }
        } catch (Exception ex) {
            // ignore shutting down exceptions
        }
    }

    public void stop() {
        try {
            if (isRunning()) {
                sensor.stop();
                scheduled.cancel(true);
                // record the result
                final var measured = new StoppedPowerMeasure(measure);
                // then set the measure to null to mark that we're ready for a new measure
                measure = null;
                // and finally, but only then, run the completion handler
                completed.accept(measured);

                powerAPI.close();
            }
        } catch (Exception e) {
            handleError(e);
        }
    }
}
