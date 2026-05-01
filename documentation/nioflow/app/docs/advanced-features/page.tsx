import type { Metadata } from "next";

export const metadata: Metadata = {
  title: "Advanced Features | NioFlow Docs",
  description: "Advanced features and production-focused capabilities in NioFlow.",
};

export default function AdvancedFeaturesPage() {
  return (
    <article className="prose prose-invert max-w-none">
      <h1>Advanced Feature Pack</h1>
      <p className="lead">
        NioFlow includes multiple opt-in features designed for small teams operating production services
        without heavy platform infrastructure. These capabilities handle resilience, observability, and
        developer experience natively within the framework.
      </p>

      <div className="not-prose my-12 space-y-8">
        <section className="rounded-xl border border-muted bg-card-dark p-6">
          <div className="flex items-center gap-3 mb-4">
            <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-red-900/30 text-red-400 font-mono text-xs font-bold border border-red-500/30">
              01
            </div>
            <h3 className="text-xl font-bold text-white">ChaosMiddleware</h3>
          </div>
          <p className="text-gray-400 mb-4 text-[15px] leading-relaxed">
            Controlled fault injection to simulate real-world networking issues and validate frontend resilience.
          </p>
          <div className="bg-black border border-muted rounded-lg p-4 font-mono text-sm text-blue-300">
            {`app.use(
  new ChaosMiddleware()
    .latency(200, 0.10)
    .error(500, 0.05)
    .drop(0.01)
);`}
          </div>
          <ul className="mt-4 space-y-2 text-sm text-gray-500 list-disc list-inside">
            <li>Guarded by <code className="text-gray-400 bg-[#222] px-1 py-0.5 rounded">NIOFLOW_CHAOS_ENABLED=true</code>.</li>
            <li>Logs each injected fault with route and path.</li>
          </ul>
        </section>

        <section className="rounded-xl border border-muted bg-card-dark p-6">
          <div className="flex items-center gap-3 mb-4">
            <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-blue-900/30 text-blue-400 font-mono text-xs font-bold border border-blue-500/30">
              02
            </div>
            <h3 className="text-xl font-bold text-white">Per-route Observability</h3>
          </div>
          <p className="text-gray-400 mb-4 text-[15px] leading-relaxed">
            Granular protection and telemetry for individual endpoints using a fluent API.
          </p>
          <div className="bg-black border border-muted rounded-lg p-4 font-mono text-sm text-blue-300">
            {`app.get("/api/orders", ordersController::list)
   .timeout(2000)
   .rateLimit(50, 10_000);`}
          </div>
          <ul className="mt-4 space-y-2 text-sm text-gray-500 list-disc list-inside">
            <li>Per-route request/error counters and sliding p50/p95/p99 latency tracking.</li>
            <li>Route-scoped timeout and rate limit enforcement.</li>
          </ul>
        </section>

        <section className="rounded-xl border border-muted bg-card-dark p-6">
          <div className="flex items-center gap-3 mb-4">
            <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-green-900/30 text-green-400 font-mono text-xs font-bold border border-green-500/30">
              03
            </div>
            <h3 className="text-xl font-bold text-white">Request Hedging</h3>
          </div>
          <p className="text-gray-400 mb-4 text-[15px] leading-relaxed">
            Tail-latency reduction for critical reads by speculatively firing a backup execution.
          </p>
          <div className="bg-black border border-muted rounded-lg p-4 font-mono text-sm text-blue-300">
            {`app.get("/api/search", searchController::search)
   .hedge(100);`}
          </div>
          <ul className="mt-4 space-y-2 text-sm text-gray-500 list-disc list-inside">
            <li>Fires a backup execution when primary crosses threshold (e.g., 100ms).</li>
            <li>Returns first successful completion to the client.</li>
          </ul>
        </section>

        <section className="rounded-xl border border-muted bg-card-dark p-6">
          <div className="flex items-center gap-3 mb-4">
            <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-orange-900/30 text-orange-400 font-mono text-xs font-bold border border-orange-500/30">
              04
            </div>
            <h3 className="text-xl font-bold text-white">Circuit Breaker</h3>
          </div>
          <p className="text-gray-400 mb-4 text-[15px] leading-relaxed">
            Prevent cascading failures to downstream services with automatic fast-failing.
          </p>
          <div className="bg-black border border-muted rounded-lg p-4 font-mono text-sm text-blue-300">
            {`app.group("/api/downstream", group -> {
  group.use(new CircuitBreakerMiddleware()
      .threshold(0.5)
      .windowSize(20)
      .cooldown(10_000));
      
  group.get("/inventory", inventoryController::read);
});`}
          </div>
          <ul className="mt-4 space-y-2 text-sm text-gray-500 list-disc list-inside">
            <li>OPEN state returns <code className="text-gray-400 bg-[#222] px-1 py-0.5 rounded">503</code> and <code className="text-gray-400 bg-[#222] px-1 py-0.5 rounded">Retry-After</code>.</li>
            <li>Metrics exposed per route-group in the `/metrics` output.</li>
          </ul>
        </section>

        <section className="rounded-xl border border-muted bg-card-dark p-6">
          <div className="flex items-center gap-3 mb-4">
            <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-purple-900/30 text-purple-400 font-mono text-xs font-bold border border-purple-500/30">
              05
            </div>
            <h3 className="text-xl font-bold text-white">Request Replay</h3>
          </div>
          <p className="text-gray-400 mb-4 text-[15px] leading-relaxed">
            Fast debugging by replaying recent recorded requests through the current live pipeline.
          </p>
          <div className="bg-black border border-muted rounded-lg p-4 font-mono text-sm text-blue-300">
            {`app.enableReplay(50);`}
          </div>
          <ul className="mt-4 space-y-2 text-sm text-gray-500 list-disc list-inside">
            <li>Guarded by <code className="text-gray-400 bg-[#222] px-1 py-0.5 rounded">NIOFLOW_REPLAY_ENABLED=true</code>.</li>
            <li>Captures requests in a memory buffer. Post to <code className="text-gray-400 bg-[#222] px-1 py-0.5 rounded">/_replay/:index</code> to re-evaluate.</li>
            <li><strong className="text-red-400">Security Warning:</strong> Replay endpoints are unauthenticated. Do not expose them in production without adding an AuthMiddleware guard, as they can leak request payloads.</li>
          </ul>
        </section>

        <section className="rounded-xl border border-muted bg-card-dark p-6">
          <div className="flex items-center gap-3 mb-4">
            <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-cyan-900/30 text-cyan-400 font-mono text-xs font-bold border border-cyan-500/30">
              06
            </div>
            <h3 className="text-xl font-bold text-white">Hot Reload</h3>
          </div>
          <p className="text-gray-400 mb-4 text-[15px] leading-relaxed">
            Near-instant developer feedback with automatic recompilation on file changes.
          </p>
          <div className="bg-black border border-muted rounded-lg p-4 font-mono text-sm text-blue-300">
            {`NioFlowApp.enableHotReload(DemoApplication.class, args);`}
          </div>
          <ul className="mt-4 space-y-2 text-sm text-gray-500 list-disc list-inside">
            <li>Guarded by <code className="text-gray-400 bg-[#222] px-1 py-0.5 rounded">NIOFLOW_WATCH=true</code>.</li>
            <li>Monitors source directory and restarts via child process.</li>
          </ul>
        </section>
      </div>
    </article>
  );
}
