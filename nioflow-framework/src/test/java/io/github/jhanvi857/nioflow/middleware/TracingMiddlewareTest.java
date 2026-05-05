package io.github.jhanvi857.nioflow.middleware;

import io.github.jhanvi857.nioflow.protocol.HttpResponse;
import io.github.jhanvi857.nioflow.protocol.HttpStatus;
import io.github.jhanvi857.nioflow.routing.HttpContext;
import io.github.jhanvi857.nioflow.routing.RouteHandler;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.trace.*;
import io.opentelemetry.context.Scope;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TracingMiddlewareTest {
    private TracingMiddleware middleware;
    private HttpContext ctx;
    private HttpResponse res;
    private RouteHandler next;
    private AtomicBoolean nextCalled;
    private Tracer mockTracer;
    private Span mockSpan;
    private SpanBuilder mockSpanBuilder;

    @BeforeEach
    void setUp() throws Exception {
        ctx = mock(HttpContext.class);
        res = mock(HttpResponse.class);
        nextCalled = new AtomicBoolean(false);
        next = (c) -> nextCalled.set(true);

        when(ctx.method()).thenReturn("GET");
        when(ctx.routePattern()).thenReturn("/test");
        when(ctx.getResponse()).thenReturn(res);
        when(res.getStatus()).thenReturn(HttpStatus.OK);

        mockTracer = mock(Tracer.class);
        mockSpan = mock(Span.class);
        mockSpanBuilder = mock(SpanBuilder.class);

        when(mockTracer.spanBuilder(anyString())).thenReturn(mockSpanBuilder);
        when(mockSpanBuilder.setSpanKind(any())).thenReturn(mockSpanBuilder);
        when(mockSpanBuilder.startSpan()).thenReturn(mockSpan);
        when(mockSpan.makeCurrent()).thenReturn(mock(Scope.class));
        
        SpanContext mockSpanContext = mock(SpanContext.class);
        when(mockSpan.getSpanContext()).thenReturn(mockSpanContext);
        when(mockSpanContext.getTraceId()).thenReturn("1234567890abcdef1234567890abcdef");

        middleware = new TracingMiddleware(mockTracer);
    }

    @Test
    void handle_tracingEnabled_setsTraceIdHeader() throws Exception {
        middleware.process(ctx, next);

        verify(ctx).header(eq("X-Trace-Id"), eq("1234567890abcdef1234567890abcdef"));
        assertTrue(nextCalled.get());
    }

    @Test
    void handle_tracingEnabled_callsNext() throws Exception {
        middleware.process(ctx, next);
        assertTrue(nextCalled.get());
        verify(mockSpan).end();
    }

    @Test
    void handle_attributesSetCorrectly() throws Exception {
        when(ctx.header("X-Forwarded-For")).thenReturn("1.2.3.4");
        middleware.process(ctx, next);

        verify(mockSpan).setAttribute(any(AttributeKey.class), eq("GET"));
        verify(mockSpan).setAttribute(any(AttributeKey.class), eq("/test"));
        verify(mockSpan).setAttribute(any(AttributeKey.class), eq("1.2.3.4"));
    }

    @Test
    void handle_clientIpFromRemoteAddress() throws Exception {
        when(ctx.header("X-Forwarded-For")).thenReturn(null);
        when(ctx.remoteAddress()).thenReturn("5.6.7.8");
        middleware.process(ctx, next);

        verify(mockSpan).setAttribute(any(AttributeKey.class), eq("5.6.7.8"));
    }

    @Test
    void handle_nextThrows_spanRecordsError() throws Exception {
        RouteHandler errorNext = (c) -> {
            throw new RuntimeException("Test error");
        };

        assertThrows(RuntimeException.class, () -> middleware.process(ctx, errorNext));

        verify(mockSpan).recordException(any(Throwable.class));
        verify(mockSpan).setStatus(eq(StatusCode.ERROR), eq("Test error"));
        verify(mockSpan).end();
    }

    @Test
    void handle_statusCode500_setsSpanError() throws Exception {
        when(res.getStatus()).thenReturn(HttpStatus.INTERNAL_SERVER_ERROR);
        middleware.process(ctx, next);

        verify(mockSpan).setStatus(eq(StatusCode.ERROR));
    }
}
