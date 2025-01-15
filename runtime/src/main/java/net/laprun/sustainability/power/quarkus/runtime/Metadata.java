package net.laprun.sustainability.power.quarkus.runtime;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import net.laprun.sustainability.power.SensorMetadata;

import java.net.URI;
import java.util.Optional;

public record Metadata(URI powerServerURI, Optional<SensorMetadata> remote, Optional<SensorMetadata> local, String status) {


    @Override
    public String toString() {
        return "Connected to " + powerServerURI + " (status: " + status
                + ")\n====\nLocal metadata (including synthetic components, if any):\n"
                + Optional.ofNullable(local).map(Object::toString).orElse("No ongoing measure")
                + "\n====\nSensor metadata:\n" + remote;
    }
}
