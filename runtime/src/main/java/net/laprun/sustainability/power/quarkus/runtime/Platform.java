package net.laprun.sustainability.power.quarkus.runtime;

import com.sun.management.OperatingSystemMXBean;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

public class Platform {
    private static final OperatingSystemMXBean os;
    private static final ThreadMXBean threads;

    static {
        os = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        threads = ManagementFactory.getThreadMXBean();
        // take two measures to avoid initial zero values
        os.getProcessCpuLoad();
        os.getCpuLoad();
        os.getProcessCpuLoad();
        os.getCpuLoad();
    }

    @SuppressWarnings("unused")
    public static double cpuShareOfJVMProcess() {
        final var processCpuLoad = os.getProcessCpuLoad();
        final var cpuLoad = os.getCpuLoad();
        return (processCpuLoad < 0 || cpuLoad <= 0) ? 0 : processCpuLoad / cpuLoad;
    }

    public static double threadCpuTime(long threadId) {
        // todo: check if available
        return threads.getThreadCpuTime(threadId);
    }

    public static double consumedThreadCpuTime(long overDurationNanos, double startThreadTime, double endThreadTime) {
        return (endThreadTime - startThreadTime) / overDurationNanos;
    }
}
