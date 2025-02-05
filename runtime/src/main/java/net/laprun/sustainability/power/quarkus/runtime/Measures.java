package net.laprun.sustainability.power.quarkus.runtime;

import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class Measures {
    private final Map<String, List<Measure>> measures = new HashMap<>();

    public Measure add(String measureName, String threadName, long threadId, long startTime, Duration duration, double threadCpu, double jvmCpu) {
        final Measure measure = new Measure(threadName, threadId, startTime, duration, threadCpu, jvmCpu);
        measures.computeIfAbsent(measureName, (unused) -> new LinkedList<>()).add(measure);
        return measure;
    }

    public Map<String, List<Measure>> measures() {
        return measures;
    }

    public record Measure(String threadName, long threadId, long startTime, Duration duration, double threadCpuShare, double jvmCpuShare) {
        @SuppressWarnings("unused")
        public String getDate() {
            return new Date(startTime).toString();
        }
    }
}
