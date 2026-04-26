package io.github.jhanvi857.nioflow.observability;

import io.github.jhanvi857.nioflow.Env;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TracerConfig {
    private static final Logger logger = LoggerFactory.getLogger(TracerConfig.class);
    private static OpenTelemetry openTelemetry = OpenTelemetry.noop();
    private static final boolean ENABLED = Env.getAsBoolean("NIOFLOW_TRACING_ENABLED", false);

    static {
        if (ENABLED) {
            try {
                String endpoint = Env.get("OTEL_EXPORTER_OTLP_ENDPOINT", "http://localhost:4317");
                
                Resource resource = Resource.getDefault()
                        .merge(Resource.create(Attributes.of(AttributeKey.stringKey("service.name"), "nioflow-app")));

                OtlpGrpcSpanExporter spanExporter = OtlpGrpcSpanExporter.builder()
                        .setEndpoint(endpoint)
                        .build();

                SdkTracerProvider tracerProvider = SdkTracerProvider.builder()
                        .addSpanProcessor(BatchSpanProcessor.builder(spanExporter).build())
                        .setResource(resource)
                        .build();

                openTelemetry = OpenTelemetrySdk.builder()
                        .setTracerProvider(tracerProvider)
                        .setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()))
                        .buildAndRegisterGlobal();
                
                logger.info("OpenTelemetry tracing enabled, exporting to {}", endpoint);
            } catch (Exception e) {
                logger.error("Failed to initialize OpenTelemetry: {}", e.getMessage());
            }
        }
    }

    public static OpenTelemetry get() {
        return openTelemetry;
    }
}
