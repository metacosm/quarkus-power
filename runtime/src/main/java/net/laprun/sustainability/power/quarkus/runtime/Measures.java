package net.laprun.sustainability.power.quarkus.runtime;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class Measures {
    private final Map<String, DisplayableMeasure> measures = new HashMap<>();

    public void add(DisplayableMeasure measure, long duration, String name) {
        measures.put(name, measure);
    }

    public Map<String, DisplayableMeasure> measures() {
        return measures;
    }
}
