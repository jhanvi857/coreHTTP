package io.github.jhanvi857.nioflow.observability;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class RouteObservabilityRegistryTest {

    @BeforeEach
    public void setUp() {
        RouteObservabilityRegistry.clearForTests();
    }

    @Test
    public void recordRequest_incrementsCounter() {
        RouteObservabilityRegistry.statsFor("GET /test").record(10, 200, false, false);
        assertEquals(1, RouteObservabilityRegistry.statsFor("GET /test").snapshot().requestCount);
    }

    @Test
    public void recordRequest_multipleRequests_counterAccumulates() {
        for (int i = 0; i < 10; i++) {
            RouteObservabilityRegistry.statsFor("GET /test").record(10, 200, false, false);
        }
        assertEquals(10, RouteObservabilityRegistry.statsFor("GET /test").snapshot().requestCount);
    }

    @Test
    public void recordRequest_concurrent_noLostIncrements() throws InterruptedException {
        int threads = 10;
        int reqsPerThread = 1000;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        for (int i = 0; i < threads; i++) {
            executor.submit(() -> {
                for (int j = 0; j < reqsPerThread; j++) {
                    RouteObservabilityRegistry.statsFor("GET /test").record(1, 200, false, false);
                }
            });
        }
        executor.shutdown();
        assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));
        assertEquals(threads * reqsPerThread, RouteObservabilityRegistry.statsFor("GET /test").snapshot().requestCount);
    }

    @Test
    public void recordLatency_p50Populated() {
        RouteObservabilityRegistry.RouteStats stats = RouteObservabilityRegistry.statsFor("GET /test");
        for (int i = 1; i <= 100; i++) {
            stats.record(i, 200, false, false);
        }
        RouteObservabilityRegistry.RouteSnapshot snapshot = stats.snapshot();
        // p50 of 1..100 should be around 50
        assertEquals(50, snapshot.p50LatencyMs);
    }

    @Test
    public void recordLatency_p95Populated() {
        RouteObservabilityRegistry.RouteStats stats = RouteObservabilityRegistry.statsFor("GET /test");
        for (int i = 1; i <= 100; i++) {
            stats.record(i, 200, false, false);
        }
        RouteObservabilityRegistry.RouteSnapshot snapshot = stats.snapshot();
        assertEquals(95, snapshot.p95LatencyMs);
    }

    @Test
    public void recordLatency_p99Populated() {
        RouteObservabilityRegistry.RouteStats stats = RouteObservabilityRegistry.statsFor("GET /test");
        for (int i = 1; i <= 100; i++) {
            stats.record(i, 200, false, false);
        }
        RouteObservabilityRegistry.RouteSnapshot snapshot = stats.snapshot();
        assertEquals(99, snapshot.p99LatencyMs);
    }

    @Test
    public void getMetrics_doesNotContainThreadNames() {
        RouteObservabilityRegistry.statsFor("GET /test").record(10, 200, false, false);
        String report = RouteObservabilityRegistry.renderTextReport();
        assertFalse(report.contains("Thread-"));
        assertFalse(report.contains("worker-"));
    }

    @Test
    public void getMetrics_doesNotContainSecretValues() {
        System.setProperty("JWT_SECRET", "super-secret-key-123");
        RouteObservabilityRegistry.statsFor("GET /test").record(10, 200, false, false);
        String report = RouteObservabilityRegistry.renderTextReport();
        assertFalse(report.contains("super-secret-key-123"));
    }

    @Test
    public void resetStats_countersReturnToZero() {
        RouteObservabilityRegistry.statsFor("GET /test").record(10, 200, false, false);
        RouteObservabilityRegistry.clearForTests();
        assertTrue(RouteObservabilityRegistry.rawStatsForTests().isEmpty());
    }
}
