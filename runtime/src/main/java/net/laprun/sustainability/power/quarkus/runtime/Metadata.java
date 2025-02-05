package net.laprun.sustainability.power.quarkus.runtime;

import java.net.URI;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

import net.laprun.sustainability.power.SensorMetadata;

public class Metadata<T> {
    private final URI powerServerURI;
    private final String status;
    private final List<T> remote;
    private final Function<SensorMetadata.ComponentMetadata, T> converter;
    private final String documentation;

    public Metadata(URI powerServerURI, String documentation, List<SensorMetadata.ComponentMetadata> remote, String status, Function<SensorMetadata.ComponentMetadata, T> converter) {
        this.powerServerURI = powerServerURI;
        this.converter = converter;
        this.remote = converted(remote);
        this.status = status;
        this.documentation = documentation;
    }

    private List<T> converted(List<SensorMetadata.ComponentMetadata> metadata) {
        return metadata.stream()
                .sorted(Comparator.comparing(SensorMetadata.ComponentMetadata::index))
                .map(converter)
                .toList();
    }

    @Override
    public String toString() {
        return "Connected to " + powerServerURI + " (status: " + status
                + ")\n====\nMetadata (including synthetic components, if any):\n"
                + remote;
    }

    public List<T> components() {
        return remote;
    }
}
