package io.github.jhanvi857.nioflow.middleware;

import io.github.jhanvi857.nioflow.observability.RouteObservabilityRegistry;
import io.github.jhanvi857.nioflow.protocol.HttpStatus;
import io.github.jhanvi857.nioflow.routing.HttpContext;
import io.github.jhanvi857.nioflow.routing.RouteHandler;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Route-group scoped circuit breaker.
 */
public class CircuitBreakerMiddleware implements Middleware {
    private enum State {
        CLOSED,
        OPEN,
        HALF_OPEN
    }

    private volatile double threshold = 0.5d;
    private volatile int windowSize = 20;
    private volatile long cooldownMs = 10_000L;

    private final Deque<Boolean> outcomes = new ArrayDeque<>();
    private final AtomicBoolean probeInFlight = new AtomicBoolean(false);
    private volatile State state = State.CLOSED;
    private volatile long openSinceMs = 0L;
    private volatile String groupKey = "default";

    public CircuitBreakerMiddleware threshold(double threshold) {
        this.threshold = Math.max(0.0d, Math.min(1.0d, threshold));
        return this;
    }

    public CircuitBreakerMiddleware windowSize(int windowSize) {
        this.windowSize = Math.max(1, windowSize);
        return this;
    }

    public CircuitBreakerMiddleware cooldown(long cooldownMs) {
        this.cooldownMs = Math.max(1L, cooldownMs);
        return this;
    }

    public CircuitBreakerMiddleware groupKey(String groupKey) {
        this.groupKey = (groupKey == null || groupKey.isBlank()) ? "default" : groupKey;
        RouteObservabilityRegistry.registerCircuitState(this.groupKey, this::state);
        return this;
    }

    public String state() {
        return state.name();
    }

    @Override
    public void process(HttpContext ctx, RouteHandler next) throws Exception {
        if (rejectIfOpen(ctx)) {
            return;
        }

        boolean probe = state == State.HALF_OPEN;
        if (probe && !probeInFlight.compareAndSet(false, true)) {
            reject(ctx);
            return;
        }

        boolean success = false;
        try {
            next.handle(ctx);
            success = ctx.getResponse().getStatus().getCode() < 500;
        } catch (Exception ex) {
            success = false;
            throw ex;
        } finally {
            onResult(success, probe);
        }
    }

    private boolean rejectIfOpen(HttpContext ctx) {
        if (state != State.OPEN) {
            return false;
        }

        long elapsed = System.currentTimeMillis() - openSinceMs;
        if (elapsed >= cooldownMs) {
            state = State.HALF_OPEN;
            return false;
        }

        reject(ctx);
        return true;
    }

    private void reject(HttpContext ctx) {
        ctx.status(HttpStatus.SERVICE_UNAVAILABLE).json(Map.of(
                "error", "Circuit Open",
                "group", groupKey,
                "retryAfterMs", cooldownMs));
        ctx.header("Retry-After", String.valueOf(cooldownMs));
    }

    private synchronized void onResult(boolean success, boolean probe) {
        if (probe) {
            probeInFlight.set(false);
            if (success) {
                state = State.CLOSED;
                outcomes.clear();
            } else {
                open();
            }
            return;
        }

        outcomes.addLast(success);
        if (outcomes.size() > windowSize) {
            outcomes.removeFirst();
        }

        if (outcomes.size() < windowSize) {
            return;
        }

        int failures = 0;
        for (Boolean ok : outcomes) {
            if (!ok) {
                failures++;
            }
        }

        double failureRate = (double) failures / (double) outcomes.size();
        if (failureRate >= threshold) {
            open();
        }
    }

    private void open() {
        state = State.OPEN;
        openSinceMs = System.currentTimeMillis();
        probeInFlight.set(false);
    }
}
