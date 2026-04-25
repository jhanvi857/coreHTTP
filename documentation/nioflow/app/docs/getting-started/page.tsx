import { CodeBlock, H2, H3, P } from "../_components";

export default function GettingStartedPage() {
  return (
    <>
      <h1 className="text-3xl md:text-4xl font-bold tracking-tight mb-4 text-gray-900 dark:text-white">Getting Started</h1>
      <P>Everything a new user needs to download, install, and run NioFlow quickly.</P>

      <H2 id="download-options">Download Options</H2>
      <H3>Maven Users (recommended)</H3>
      <P>Maven gives dependency management, reproducible builds, and easiest upgrades.</P>
      <CodeBlock
        title="pom.xml"
        language="xml"
        code={`<dependency>
  <groupId>io.github.jhanvi857</groupId>
  <artifactId>nioflow-framework</artifactId>
  <version>1.0.0</version>
</dependency>`}
      />

      <H3>Without Maven</H3>
      <P>Download nioflow-framework-1.0.0.jar and checksum from GitHub Releases.</P>
      <CodeBlock
        title="manual-jar"
        language="bash"
        code={`# Linux/macOS
javac -cp nioflow-framework-1.0.0.jar App.java
java -cp nioflow-framework-1.0.0.jar:. App

# Windows PowerShell
javac -cp .\\nioflow-framework-1.0.0.jar App.java
java -cp .\\nioflow-framework-1.0.0.jar;. App`}
      />

      <H2 id="setup-project">Setup Your First App</H2>
      <CodeBlock
        title="App.java"
        language="java"
        code={`import io.github.jhanvi857.nioflow.NioFlowApp;
import io.github.jhanvi857.nioflow.middleware.ChaosMiddleware;
import io.github.jhanvi857.nioflow.middleware.CircuitBreakerMiddleware;
import io.github.jhanvi857.nioflow.protocol.HttpStatus;

public class App {
    public static void main(String[] args) {
        NioFlowApp app = new NioFlowApp();

        app.use(new ChaosMiddleware().latency(120, 0.05));

        app.get("/", ctx -> ctx.send("NioFlow is running"));
        app.get("/api/search", searchController::search)
           .timeout(1500)
           .rateLimit(40, 10_000)
           .hedge(100);

        app.group("/api/downstream", group -> {
            group.use(new CircuitBreakerMiddleware().threshold(0.5).windowSize(20).cooldown(10_000));
            group.get("/inventory", inventoryController::read);
        });

        app.enableReplay(50);
        app.get("/_health", ctx -> ctx.status(HttpStatus.OK).json(java.util.Map.of("status", "UP")));

        app.listen(8080);
    }
}`}
      />

      <H2 id="feature-flags">Feature Flags (Safe Defaults)</H2>
      <CodeBlock
        title=".env"
        language="bash"
        code={`# disabled by default
NIOFLOW_CHAOS_ENABLED=false
NIOFLOW_REPLAY_ENABLED=false

# enable intentionally in non-prod debugging sessions
# NIOFLOW_CHAOS_ENABLED=true
# NIOFLOW_REPLAY_ENABLED=true`}
      />

      <H2 id="port-config">Port Registration</H2>
      <P>In cloud providers, use PORT env var. In local, default to 8080.</P>
      <CodeBlock
        title="port-config"
        language="java"
        code={`int port = 8080;
String value = System.getenv("PORT");
if (value != null && !value.isBlank()) {
    port = Integer.parseInt(value);
}
app.listen(port);`}
      />

      <H2 id="project-layout">Suggested Layout</H2>
      <CodeBlock
        title="project-structure"
        language="text"
        code={`my-app/
  src/main/java/com/example/
    App.java
    controller/
      TaskController.java
    auth/
      AuthController.java
    repository/
      TaskRepository.java
    model/
      Task.java
  src/main/resources/public/
    index.html
  pom.xml`}
      />
    </>
  );
}
