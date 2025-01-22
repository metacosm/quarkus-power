package net.laprun.sustainability.power.quarkus.runtime;

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

    @Inject
    Measures measures;

    @AroundInvoke
    public Object aroundInvoke(InvocationContext ctx) throws Exception {
        final var measureAnn = ctx.getInterceptorBinding(PowerMeasure.class);
        if(measureAnn == null) {
            return ctx.proceed();
        }

        final var startTime = System.nanoTime();
        try {
            measurer.start(0, 100);
            return ctx.proceed();
        } finally {
            final var duration = System.nanoTime() - startTime;
            final var stop = measurer.stop();
            stop.ifPresent(measure -> measures.add(measure, duration, measureAnn.name()));
        }
    }
}
