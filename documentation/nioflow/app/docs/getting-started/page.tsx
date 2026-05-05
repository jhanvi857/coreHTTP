import { CodeBlock, H2, H3, P, Pagination } from "../_components";

export default function GettingStartedPage() {
  return (
    <>
      <h1 className="text-3xl md:text-4xl font-bold tracking-tight mb-4 text-gray-900 dark:text-white">Getting Started</h1>
      <P>Everything a new user needs to download, install, and run NioFlow quickly.</P>

      <H2 id="prerequisites">Prerequisites</H2>
      <P>Before starting, ensure you have the following installed on your machine:</P>
      <ul className="list-disc pl-6 space-y-2 my-4 text-gray-700 dark:text-gray-300">
        <li><strong>Java 17+:</strong> <a href="https://adoptium.net/" target="_blank" className="text-blue-500 hover:underline">Download Eclipse Temurin</a></li>
        <li><strong>Node.js (for CLI):</strong> <a href="https://nodejs.org/" target="_blank" className="text-blue-500 hover:underline">Download Node.js</a></li>
      </ul>

      <H2 id="download-options">Installation</H2>
      <H3>NioFlow CLI (Recommended)</H3>
      <P>The fastest way to get started. The CLI handles scaffolding, environment setup, and Maven management for you.</P>
      <CodeBlock
        title="install-cli"
        language="bash"
        code={`# Install globally
npm install -g @jhanvi857/nioflow-cli

# Scaffold a new project
nioflow new my-app
cd my-app

# Start development with hot-reload
nioflow dev`}
      />

      <H3>Manual Maven Dependency</H3>
      <P>If you prefer managing your own pom.xml, add the dependency directly.</P>
      <CodeBlock
        title="pom.xml"
        language="xml"
        code={`<dependency>
  <groupId>io.github.jhanvi857</groupId>
  <artifactId>nioflow-framework</artifactId>
  <version>1.4.0</version>
</dependency>`}
      />



      <H2 id="setup-project">Setup Your First App</H2>
      <CodeBlock
        title="App.java"
        language="java"
        code={`import io.github.jhanvi857.nioflow.NioFlowApp;

public class App {
    public static void main(String[] args) {
        NioFlowApp app = new NioFlowApp();

        app.get("/", ctx -> ctx.send("NioFlow is running"));

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
NIOFLOW_WATCH=false

# enable intentionally in non-prod debugging sessions
# NIOFLOW_CHAOS_ENABLED=true
# NIOFLOW_REPLAY_ENABLED=true
# NIOFLOW_WATCH=true
`}
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

      <Pagination 
        prev={{ href: "/docs", label: "Professional Framework Guide" }}
        next={{ href: "/docs/routing-frontend", label: "Routing + Frontend" }}
      />
    </>
  );
}
