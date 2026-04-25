package io.github.jhanvi857.nioflow.observability;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Supplier;

/**
 * In-memory route metrics and circuit state registry with no external dependencies.
 */
public final class RouteObservabilityRegistry {
    private static final int LATENCY_WINDOW_SIZE = 512;
    private static final ConcurrentHashMap<String, RouteStats> ROUTE_STATS = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Supplier<String>> CIRCUIT_STATES = new ConcurrentHashMap<>();

    private RouteObservabilityRegistry() {
    }

    public static RouteStats statsFor(String routeKey) {
        return ROUTE_STATS.computeIfAbsent(routeKey, k -> new RouteStats());
    }

    public static void registerCircuitState(String groupKey, Supplier<String> stateSupplier) {
        CIRCUIT_STATES.put(groupKey, stateSupplier);
    }

    public static String renderTextReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("# nioflow_route_metrics\n");
        List<String> keys = new ArrayList<>(ROUTE_STATS.keySet());
        Collections.sort(keys);
        for (String key : keys) {
            RouteSnapshot snapshot = ROUTE_STATS.get(key).snapshot();
            sb.append("route=").append(key)
              .append(" requests=").append(snapshot.requestCount)
              .append(" errors=").append(snapshot.errorCount)
              .append(" timeouts=").append(snapshot.timeoutCount)
              .append(" hedges=").append(snapshot.hedgeCount)
              .append(" p50_ms=").append(snapshot.p50LatencyMs)
              .append(" p95_ms=").append(snapshot.p95LatencyMs)
              .append(" p99_ms=").append(snapshot.p99LatencyMs)
              .append("\n");
        }

        sb.append("# nioflow_circuit_states\n");
        List<String> groups = new ArrayList<>(CIRCUIT_STATES.keySet());
        Collections.sort(groups);
        for (String group : groups) {
            Supplier<String> supplier = CIRCUIT_STATES.get(group);
            String state = supplier != null ? supplier.get() : "UNKNOWN";
            sb.append("group=").append(group).append(" state=").append(state).append("\n");
        }

        return sb.toString();
    }

    public static void clearForTests() {
        ROUTE_STATS.clear();
        CIRCUIT_STATES.clear();
    }

    public static final class RouteStats {
        private final LongAdder requestCount = new LongAdder();
        private final LongAdder errorCount = new LongAdder();
        private final LongAdder timeoutCount = new LongAdder();
        private final LongAdder hedgeCount = new LongAdder();
        private final Deque<Long> latencyWindowMs = new ArrayDeque<>();

        public void record(long latencyMs, int statusCode, boolean timeout, boolean hedgeTriggered) {
            requestCount.increment();
            if (statusCode >= 400) {
                errorCount.increment();
            }
            if (timeout) {
                timeoutCount.increment();
            }
            if (hedgeTriggered) {
                hedgeCount.increment();
            }

            synchronized (latencyWindowMs) {
                latencyWindowMs.addLast(Math.max(0, latencyMs));
                if (latencyWindowMs.size() > LATENCY_WINDOW_SIZE) {
                    latencyWindowMs.removeFirst();
                }
            }
        }

        public RouteSnapshot snapshot() {
            List<Long> latencies;
            synchronized (latencyWindowMs) {
                latencies = new ArrayList<>(latencyWindowMs);
            }
            latencies.sort(Comparator.naturalOrder());

            return new RouteSnapshot(
                    requestCount.sum(),
                    errorCount.sum(),
                    timeoutCount.sum(),
                    hedgeCount.sum(),
                    percentile(latencies, 0.50),
                    percentile(latencies, 0.95),
                    percentile(latencies, 0.99));
        }

        private static long percentile(List<Long> sorted, double p) {
            if (sorted.isEmpty()) {
                return 0;
            }
            int idx = (int) Math.ceil(p * sorted.size()) - 1;
            idx = Math.min(Math.max(idx, 0), sorted.size() - 1);
            return sorted.get(idx);
        }
    }

    public static final class RouteSnapshot {
        public final long requestCount;
        public final long errorCount;
        public final long timeoutCount;
        public final long hedgeCount;
        public final long p50LatencyMs;
        public final long p95LatencyMs;
        public final long p99LatencyMs;

        public RouteSnapshot(long requestCount, long errorCount, long timeoutCount, long hedgeCount,
                long p50LatencyMs, long p95LatencyMs, long p99LatencyMs) {
            this.requestCount = requestCount;
            this.errorCount = errorCount;
            this.timeoutCount = timeoutCount;
            this.hedgeCount = hedgeCount;
            this.p50LatencyMs = p50LatencyMs;
            this.p95LatencyMs = p95LatencyMs;
            this.p99LatencyMs = p99LatencyMs;
        }
    }

    public static Map<String, RouteStats> rawStatsForTests() {
        return ROUTE_STATS;
    }
}
