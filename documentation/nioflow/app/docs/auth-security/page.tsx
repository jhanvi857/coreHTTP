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
        code={`import com.jhanvi857.nioflow.auth.PasswordHasher;
import com.jhanvi857.nioflow.auth.JwtProvider;

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
    tasks.use(new com.jhanvi857.nioflow.middleware.AuthMiddleware());

    tasks.get("/", taskController::list);
    tasks.post("/", taskController::create);
    tasks.get("/:id", taskController::get);
    tasks.delete("/:id", taskController::delete);
});`}
      />

      <H2 id="security-baseline">Security Baseline</H2>
      <CodeBlock
        title="env-security"
        language="bash"
        code={`JWT_SECRET=replace-with-32-plus-char-secret
NIOFLOW_CORS_ORIGIN=https://your-frontend.app
NIOFLOW_ENABLE_DB=false
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
