import { CodeBlock, H2, P, Pagination } from "../_components";

export default function AuthSecurityPage() {
  return (
    <>
      <h1 className="text-3xl md:text-4xl font-bold tracking-tight mb-4 text-gray-900 dark:text-white">Authentication + Security</h1>
      <P>Implement signup/login, JWT issuance, and route protection in a production-friendly way.</P>

      <H2 id="auth-login-signup">Signup + Login Flow</H2>
      <P><strong>What is a JWT?</strong> A JSON Web Token (JWT) is a securely signed string that the server generates upon successful login. The client stores this token and sends it back in the <code className="bg-black/30 px-1 rounded">Authorization: Bearer &lt;token&gt;</code> header with every subsequent request to prove their identity statelessly.</P>
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
    tasks.get("/:id", ctx -> {
        long id = ctx.pathParamAsLong("id"); // Type-safe parameter extraction
        // ...
    });
});`}
      />

      <H2 id="jwt-hardening">JWT Hardening (v1.4.0)</H2>
      <P>NioFlow v1.4.0 implements several enterprise-grade security controls for JWT issuance and validation:</P>
      <ul className="list-disc ml-6 space-y-2 text-gray-700 dark:text-gray-300">
        <li><strong>Issuer Pinning:</strong> All tokens are pinned to the <code className="bg-black/30 px-1 rounded">nioflow</code> issuer. Validation fails if the <code className="bg-black/30 px-1 rounded">iss</code> claim is missing or mismatched.</li>
        <li><strong>Entropy Enforcement:</strong> The framework validates the Shannon entropy of your <code className="bg-black/30 px-1 rounded">JWT_SECRET</code> at startup to prevent weak keys.</li>
        <li><strong>Short-lived Tokens:</strong> Default expiration is reduced to 15 minutes (configurable via <code className="bg-black/30 px-1 rounded">NIOFLOW_JWT_EXPIRATION_MS</code>).</li>
        <li><strong>Replay Protection:</strong> Every token includes a unique <code className="bg-black/30 px-1 rounded">jti</code> (JWT ID) claim.</li>
      </ul>

      <H2 id="security-baseline">Security Baseline</H2>
      <div className="my-4 border-l-4 border-red-500 bg-red-900/20 p-4 rounded-r-lg">
        <p className="text-red-400 font-bold mb-1">CRITICAL: Never disable auth in production</p>
        <p className="text-sm text-red-300">
          The <code className="bg-black/30 px-1 rounded">NIOFLOW_DISABLE_AUTH=true</code> flag is for development only. <strong>As of v1.4.0, the framework will refuse to start with this flag enabled unless bound to a loopback address (127.0.0.1/localhost).</strong>
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

      <H2 id="http-parser-hardening">HTTP Parser Hardening</H2>
      <P>The internal parser includes active defenses against common web vulnerabilities:</P>
      <ul className="list-disc ml-6 space-y-2 text-gray-700 dark:text-gray-300">
        <li><strong>CRLF Injection:</strong> Header values containing carriage return or line feed characters are rejected with a 400 Bad Request.</li>
        <li><strong>Null Byte Defense:</strong> Null bytes (<code className="bg-black/30 px-1 rounded">\x00</code>) are prohibited in request paths and headers.</li>
        <li><strong>Request Smuggling:</strong> Obfuscated Transfer-Encoding headers (e.g., <code className="bg-black/30 px-1 rounded">identity, chunked</code>) are detected and rejected.</li>
      </ul>
      <Pagination
        prev={{ href: "/docs/routing-frontend", label: "Routing + Frontend" }}
        next={{ href: "/docs/database-env", label: "Database + Env" }}
      />
    </>
  );
}
