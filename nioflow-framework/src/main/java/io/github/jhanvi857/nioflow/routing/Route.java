package io.github.jhanvi857.nioflow.routing;

import io.github.jhanvi857.nioflow.observability.RouteObservabilityRegistry;
import io.github.jhanvi857.nioflow.protocol.HttpStatus;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Route {
    private static final ScheduledExecutorService SCHEDULER = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "nioflow-route-policy");
        t.setDaemon(true);
        return t;
    });

    private final String method;
    private final String pathDefinition;
    private final Pattern pattern;
    private final List<String> paramNames;
    private RouteHandler handler;
    private volatile int timeoutMs;
    private volatile int hedgeDelayMs;
    private volatile RouteLimiter routeLimiter;

    public Route(String method, String pathDefinition, RouteHandler handler) {
        this.method = method.toUpperCase();
        this.pathDefinition = pathDefinition;
        this.handler = handler;
        this.paramNames = new ArrayList<>();

        String regex = pathDefinition;

        // Handle wildcard /* -> matches everything after
        if (regex.endsWith("/*")) {
            regex = regex.substring(0, regex.length() - 2) + "(?:/.*)?";
        }

        // Extract :paramName
        Matcher m = Pattern.compile(":([a-zA-Z0-9_]+)").matcher(regex);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            paramNames.add(m.group(1));
            m.appendReplacement(sb, "([^/]+)");
        }
        m.appendTail(sb);

        String finalRegex = sb.toString();
        if (finalRegex.equals("/")) {
            finalRegex = "^/$";
        } else if (!finalRegex.contains(".*")) {
            finalRegex = "^" + finalRegex + "/?$";
        } else {
            finalRegex = "^" + finalRegex + "$";
        }

        this.pattern = Pattern.compile(finalRegex);
    }

    public boolean matches(String requestMethod, String requestPath) {
        if (!this.method.equals(requestMethod) && !this.method.equals("ANY")) {
            return false;
        }
        return matchesPath(requestPath);
    }

    public boolean matchesPath(String requestPath) {
        return pattern.matcher(requestPath).matches();
    }

    public Map<String, String> extractPathParams(String requestPath) {
        Map<String, String> params = new HashMap<>();
        Matcher m = pattern.matcher(requestPath);
        if (m.matches()) {
            for (int i = 0; i < paramNames.size(); i++) {
                params.put(paramNames.get(i), m.group(i + 1));
            }
        }
        return params;
    }

    public RouteHandler getHandler() {
        return handler;
    }

    public void wrap(io.github.jhanvi857.nioflow.middleware.Middleware middleware) {
        RouteHandler inner = this.handler;
        this.handler = ctx -> middleware.process(ctx, inner);
    }

    public String key() {
        return method + " " + pathDefinition;
    }

    public Route timeout(int ms) {
        this.timeoutMs = Math.max(0, ms);
        return this;
    }

    public Route rateLimit(int requests, int windowMs) {
        this.routeLimiter = new RouteLimiter(requests, windowMs);
        return this;
    }

    public Route hedge(int delayMs) {
        this.hedgeDelayMs = Math.max(0, delayMs);
        return this;
    }

    public void execute(HttpContext ctx, ExecutorService routeExecutor) throws Exception {
        long start = System.nanoTime();
        boolean timeoutTriggered = false;
        boolean hedgeTriggered = false;

        if (routeLimiter != null && !routeLimiter.allow()) {
            ctx.status(HttpStatus.TOO_MANY_REQUESTS).json(Map.of("error", "Too Many Requests"));
            record(ctx, start, timeoutTriggered, hedgeTriggered);
            return;
        }

        try {
            if (hedgeDelayMs > 0 && routeExecutor != null) {
                hedgeTriggered = executeWithHedge(ctx, routeExecutor);
                return;
            }

            if (timeoutMs > 0 && routeExecutor != null) {
                timeoutTriggered = executeWithTimeout(ctx, routeExecutor);
                return;
            }

            handler.handle(ctx);
        } finally {
            record(ctx, start, timeoutTriggered, hedgeTriggered);
        }
    }

    private void record(HttpContext ctx, long startNanos, boolean timeoutTriggered, boolean hedgeTriggered) {
        long latencyMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos);
        RouteObservabilityRegistry.statsFor(key()).record(
                latencyMs,
                ctx.getResponse().getStatus().getCode(),
                timeoutTriggered,
                hedgeTriggered);
    }

    private boolean executeWithTimeout(HttpContext ctx, ExecutorService executor) throws Exception {
        HttpContext isolated = ctx.fork();
        Future<?> future;
        try {
            future = executor.submit(() -> {
                handler.handle(isolated);
                return null;
            });
        } catch (RejectedExecutionException ex) {
            ctx.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .json(Map.of("error", "Route execution queue is full"));
            return false;
        }

        try {
            future.get(timeoutMs, TimeUnit.MILLISECONDS);
            ctx.setResponse(isolated.getResponse());
            return false;
        } catch (java.util.concurrent.TimeoutException timeout) {
            future.cancel(true);
            ctx.status(HttpStatus.REQUEST_TIMEOUT).json(Map.of("error", "Request Timeout"));
            return true;
        }
    }

    private boolean executeWithHedge(HttpContext ctx, ExecutorService executor) throws Exception {
        HttpContext primary = ctx.fork();
        CompletableFuture<HttpContext> primaryFuture = CompletableFuture.supplyAsync(() -> runSafely(primary),
                executor);

        CompletableFuture<HttpContext> hedgeFuture = new CompletableFuture<>();
        SCHEDULER.schedule(() -> {
            if (!primaryFuture.isDone()) {
                HttpContext hedgeCtx = ctx.fork();
                CompletableFuture.supplyAsync(() -> runSafely(hedgeCtx), executor)
                        .whenComplete((result, ex) -> {
                            if (ex != null) {
                                hedgeFuture.completeExceptionally(ex);
                            } else {
                                hedgeFuture.complete(result);
                            }
                        });
            }
        }, hedgeDelayMs, TimeUnit.MILLISECONDS);

        CompletableFuture<Object> winner = CompletableFuture.anyOf(primaryFuture, hedgeFuture);
        Object completed = winner.get(timeoutMs > 0 ? timeoutMs : 30_000, TimeUnit.MILLISECONDS);

        if (completed instanceof HttpContext) {
            ctx.setResponse(((HttpContext) completed).getResponse());
        }
        boolean hedgeWasFired = hedgeFuture.isDone();
        primaryFuture.cancel(true);
        hedgeFuture.cancel(true);
        return hedgeWasFired;
    }

    private HttpContext runSafely(HttpContext ctx) {
        try {
            handler.handle(ctx);
            return ctx;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static class RouteLimiter {
        private final int requests;
        private final long windowMs;
        private final Deque<Long> timestamps = new ArrayDeque<>();

        private RouteLimiter(int requests, int windowMs) {
            this.requests = Math.max(1, requests);
            this.windowMs = Math.max(1, windowMs);
        }

        private synchronized boolean allow() {
            long now = System.currentTimeMillis();
            while (!timestamps.isEmpty() && now - timestamps.peekFirst() > windowMs) {
                timestamps.removeFirst();
            }
            if (timestamps.size() >= requests) {
                return false;
            }
            timestamps.addLast(now);
            return true;
        }
    }
}
