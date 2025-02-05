package net.laprun.sustainability.power.quarkus.runtime;

import java.time.Duration;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;

import net.laprun.sustainability.power.quarkus.annotations.PowerMeasure;

@PowerMeasure(name = "net.laprun.sustainability.power.interceptor")
@Priority(Interceptor.Priority.PLATFORM_BEFORE)
@Interceptor
public class PowerMeasuredInterceptor {
    @Inject
    PowerMeasurer measurer;

    @AroundInvoke
    @SuppressWarnings("unused")
    public Object aroundInvoke(InvocationContext ctx) throws Exception {
        final var measureAnn = ctx.getInterceptorBinding(PowerMeasure.class);
        if(measureAnn == null || !measurer.isRunning()) {
            return ctx.proceed();
        }

        final var startTimeMs = System.currentTimeMillis();
        final var startTime = System.nanoTime();
        final var thread = Thread.currentThread();
        final var threadName = thread.getName();
        final var threadId = thread.threadId();
        final var startCPU = Platform.threadCpuTime(threadId);
        try {
            return ctx.proceed();
        } finally {
            final var duration = System.nanoTime() - startTime;
            final var cpu = Platform.consumedThreadCpuTime(duration, startCPU, Platform.threadCpuTime(threadId));
            measurer.recordMethodMeasure(measureAnn.name(), threadName, threadId, startTimeMs, Duration.ofNanos(duration), cpu);
        }
    }
}
