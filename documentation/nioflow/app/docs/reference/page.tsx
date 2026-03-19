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

app.get(String path, Handler handler);
app.post(String path, Handler handler);
app.put(String path, Handler handler);
app.delete(String path, Handler handler);

app.group(String prefix, GroupConfig config);
app.exception(Class<? extends Throwable> type, ExceptionHandler handler);
app.onError(GlobalErrorHandler handler);

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
        code={`app.use(new RequestLoggerMiddleware());
app.use(new RateLimitMiddleware());
app.use(new AuthMiddleware());

// order matters: logger -> rate limit -> auth -> route handler`}
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
    </>
  );
}
