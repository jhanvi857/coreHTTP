import { CodeBlock, H2, P } from "../_components";

export default function ReferencePage() {
  return (
    <>
      <h1 className="text-3xl md:text-4xl font-bold tracking-tight mb-4 text-gray-900 dark:text-white">Reference</h1>
      <P>Compact reference for key APIs, middleware hooks, and utility functions.</P>

      <H2 id="core-app-api">Core App API</H2>
      <CodeBlock
        title="nioflow-app-api"
        language="java"
        code={`NioFlowApp app = new NioFlowApp();

    RouteRegistration route = app.get(String path, Handler handler);
    route.timeout(int ms);
    route.rateLimit(int requests, int windowMs);
    route.hedge(int delayMs);

    app.post(String path, Handler handler);
    app.put(String path, Handler handler);
    app.delete(String path, Handler handler);

app.group(String prefix, GroupConfig config);
app.exception(Class<? extends Throwable> type, ExceptionHandler handler);
app.onError(GlobalErrorHandler handler);
    app.enableReplay(int capacity);

app.listen(int port);`}
      />

      <H2 id="context-api">Context API</H2>
      <CodeBlock
        title="context-methods"
        language="java"
        code={`String value = ctx.pathParam("id");
String query = ctx.queryParam("q");
String auth = ctx.header("Authorization");

MyBody body = ctx.body(MyBody.class);

ctx.status(200);
ctx.header("X-Trace-Id", "abc");
ctx.json(java.util.Map.of("ok", true));
ctx.send("plain text");`}
      />

      <H2 id="middleware">Middleware Chain</H2>
      <CodeBlock
        title="middleware-order"
        language="java"
        code={`app.use(new LoggerMiddleware());
app.use(new ChaosMiddleware().latency(150, 0.05));
app.use(new RateLimitMiddleware(100, 10_000));

// order matters: logger -> chaos -> global limiter -> route/group policies`}
      />

      <H2 id="circuit-breaker">Circuit Breaker (Group Scoped)</H2>
      <CodeBlock
        title="circuit-breaker"
        language="java"
        code={`app.group("/api/downstream", group -> {
    group.use(new CircuitBreakerMiddleware()
        .threshold(0.5)
        .windowSize(20)
        .cooldown(10_000));

    group.get("/inventory", inventoryController::read);
});`}
      />

      <H2 id="replay-api">Request Replay API</H2>
      <CodeBlock
        title="replay-api"
        language="text"
        code={`Enable:
NIOFLOW_REPLAY_ENABLED=true
app.enableReplay(50)

Endpoints:
GET  /_replay
POST /_replay/:index

Sensitive headers stripped automatically:
- Authorization
- Cookie
- X-API-Key`}
      />

      <H2 id="auth-utils">Auth Utilities</H2>
      <CodeBlock
        title="auth-utils"
        language="java"
        code={`String hash = PasswordHasher.hash("secret-password");
boolean ok = PasswordHasher.verify("secret-password", hash);

String token = JwtProvider.generateToken("user@example.com", "USER");
var claims = JwtProvider.validateToken(token);`}
      />

      <H2 id="status-codes">HTTP Status Utilities</H2>
      <CodeBlock
        title="http-status"
        language="java"
        code={`ctx.status(HttpStatus.OK).json(java.util.Map.of("status", "ok"));
ctx.status(HttpStatus.CREATED).json(java.util.Map.of("id", 1));
ctx.status(HttpStatus.BAD_REQUEST).json(java.util.Map.of("error", "invalid"));
ctx.status(HttpStatus.UNAUTHORIZED).json(java.util.Map.of("error", "auth required"));`}
      />

      <H2 id="metrics-output">Metrics Output</H2>
      <CodeBlock
        title="metrics"
        language="text"
        code={`GET /metrics includes:
- global middleware counters
- per-route request/error/timeout/hedge counts
- per-route p50/p95/p99 latencies
- circuit breaker state per route-group`}
      />
    </>
  );
}
