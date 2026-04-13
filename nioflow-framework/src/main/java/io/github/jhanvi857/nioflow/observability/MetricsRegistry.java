package io.github.jhanvi857.nioflow.observability;

import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;

/**
 * Global metrics registry for NioFlow.
 */
public class MetricsRegistry {
    private static final PrometheusMeterRegistry registry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);

    static {
        // Bind JVM metrics by default
        new JvmThreadMetrics().bindTo(registry);
        new JvmMemoryMetrics().bindTo(registry);
        try (JvmGcMetrics gcMetrics = new JvmGcMetrics()) {
            gcMetrics.bindTo(registry);
        }
        new ProcessorMetrics().bindTo(registry);
    }

    public static PrometheusMeterRegistry getRegistry() {
        return registry;
    }

    /**
     * Returns the metrics in Prometheus format.
     */
    public static String scrape() {
        return registry.scrape();
    }
}
