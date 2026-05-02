import { CodeBlock, H2, P } from "../_components";

export default function AuthSecurityPage() {
  return (
    <>
      <h1 className="text-3xl md:text-4xl font-bold tracking-tight mb-4 text-gray-900 dark:text-white">Authentication + Security</h1>
      <P>Implement signup/login, JWT issuance, and route protection in a production-friendly way.</P>

      <H2 id="auth-login-signup">Signup + Login Flow</H2>
      <CodeBlock
        title="auth-routes"
        language="java"
        code={`import io.github.jhanvi857.nioflow.auth.PasswordHasher;
import io.github.jhanvi857.nioflow.auth.JwtProvider;

app.post("/api/auth/signup", ctx -> {
    SignupRequest req = ctx.body(SignupRequest.class);
    String hash = PasswordHasher.hash(req.getPassword());
    // save user + hash into repository
    ctx.status(201).json(java.util.Map.of("message", "user created"));
});

app.post("/api/auth/login", ctx -> {
    LoginRequest req = ctx.body(LoginRequest.class);
    boolean ok = PasswordHasher.verify(req.getPassword(), storedHash);
    if (!ok) {
        ctx.status(401).json(java.util.Map.of("error", "Invalid credentials"));
        return;
    }
    String token = JwtProvider.generateToken(userEmail, "USER");
    ctx.status(200).json(java.util.Map.of("token", token));
});`}
      />

      <H2 id="protect-routes">Protect Route Groups</H2>
      <CodeBlock
        title="protected-routes"
        language="java"
        code={`app.group("/api/tasks", tasks -> {
    tasks.use(new io.github.jhanvi857.nioflow.middleware.AuthMiddleware());

    tasks.get("/", taskController::list).rateLimit(30, 10_000);
    tasks.post("/", taskController::create);
    tasks.get("/:id", taskController::get).timeout(1200);
    tasks.delete("/:id", taskController::delete);
});`}
      />

      <H2 id="security-baseline">Security Baseline</H2>
      <div className="my-4 border-l-4 border-red-500 bg-red-900/20 p-4 rounded-r-lg">
        <p className="text-red-400 font-bold mb-1">⚠️ CRITICAL: Never disable auth in production</p>
        <p className="text-sm text-red-300">
          The <code className="bg-black/30 px-1 rounded">NIOFLOW_DISABLE_AUTH=true</code> flag is documented for local development convenience. <strong>It bypasses all JWT validation.</strong> You must never set this in a production environment, as it leaves all protected routes fully exposed.
        </p>
      </div>
      <CodeBlock
        title="env-security"
        language="bash"
        code={`JWT_SECRET=replace-with-32-plus-char-secret
NIOFLOW_CORS_ORIGIN=https://your-frontend.app
NIOFLOW_ENABLE_DB=false
NIOFLOW_CHAOS_ENABLED=false
NIOFLOW_REPLAY_ENABLED=false
NIOFLOW_EXPOSE_ERROR_DETAILS=false`}
      />

      <H2 id="error-handling">Error Handling Policy</H2>
      <CodeBlock
        title="global-errors"
        language="java"
        code={`app.exception(IllegalArgumentException.class, (e, ctx) -> {
    ctx.status(400).json(java.util.Map.of("error", "Bad Request"));
});

app.onError((err, ctx) -> {
    ctx.status(500).json(java.util.Map.of("error", "Internal Server Error"));
});`}
      />
    </>
  );
}
