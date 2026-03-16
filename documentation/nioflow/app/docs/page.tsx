import Footer from "../components/Footer";
import Navbar from "../components/Navbar";
import { configRows, endpoints } from "../lib/docsContent";

const H2 = ({ id, children }: { id: string; children: React.ReactNode }) => (
  <h2 id={id} className="text-2xl font-semibold tracking-tight text-primary mt-20 mb-6 scroll-mt-28 border-b border-muted pb-4">
    {children}
  </h2>
);

const H3 = ({ children }: { children: React.ReactNode }) => (
  <h3 className="text-lg font-semibold tracking-tight text-primary mt-12 mb-4">{children}</h3>
);

const CodeBlock = ({ code, language = "bash", title }: { code: string, language?: string, title?: string }) => {
  const getTokenColor = (token: string) => {
    const t = token.trim();
    if (/^(public|class|static|void|new|return|import|const|function|CREATE|TABLE|IF|NOT|EXISTS|PRIMARY|KEY|DEFAULT|SERIAL|INT|DECIMAL|VARCHAR|TIMESTAMP|CURRENT_TIMESTAMP|docker-compose|up)$/.test(t)) return "text-[#f97583]";
    if (/^(NioFlowApp|CompletableFuture|ExecutorService|Executors|Connection|PreparedStatement|ResultSet|SQLException|CompletionException|TimeUnit|Thread|Map|List|Runtime|String|HttpStatus|System|out|req|res)$/.test(t)) return "text-[#b392f0]";
    if (t.startsWith('"') || t.startsWith("'") || t.startsWith('`')) return "text-[#9ecbff]";
    return "";
  };

  return (
    <div className="my-6 rounded-xl border border-muted bg-[#0e0e11] overflow-hidden shadow-md">
      <div className="flex items-center px-4 py-3 border-b border-[#222] bg-[#1a1a1e] relative">
        <div className="flex space-x-2 absolute left-4">
          <div className="w-3 h-3 rounded-full bg-[#ff5f56]"></div>
          <div className="w-3 h-3 rounded-full bg-[#ffbd2e]"></div>
          <div className="w-3 h-3 rounded-full bg-[#27c93f]"></div>
        </div>
        <div className="w-full text-center">
          <span className="text-xs font-mono text-gray-400">{title || language}</span>
        </div>
      </div>
      <div className="overflow-x-auto p-4 md:p-6 pb-6 text-[13px] md:text-sm leading-relaxed">
        <pre className="text-gray-300 font-mono">
          {code.split('\n').map((line, i) => {
            const isComment = line.trim().startsWith('//') || line.trim().startsWith('--');
            return (
              <div key={i} className="table-row">
                <span className="table-cell select-none pr-6 text-right text-gray-600 border-r border-[#333]">{i + 1}</span>
                <span className="table-cell whitespace-pre pl-6">
                  {isComment ? (
                    <span className="italic text-[#6a737d]">{line}</span>
                  ) : (
                    line.split(/(\s+|[,;{}().])/g).map((part, j) => (
                      <span key={j} className={part ? getTokenColor(part) : ""}>{part}</span>
                    ))
                  )}
                </span>
              </div>
            );
          })}
        </pre>
      </div>
    </div>
  );
};

const SidebarLink = ({ href, children }: { href: string; children: React.ReactNode }) => (
  <li>
    <a
      href={href}
      className="block py-1.5 text-[14px] text-gray-500 hover:text-black dark:text-gray-400 dark:hover:text-white transition-colors"
    >
      {children}
    </a>
  </li>
);

export default function DocsPage() {
  return (
    <div className="app-shell bg-primary text-primary min-h-screen">
      <Navbar />

      {/* Subtle background grid pattern for visual depth */}
      <div className="absolute inset-0 site-grid-bg pointer-events-none" />

      <div className="w-full max-w-[1400px] mx-auto flex flex-col md:flex-row relative z-10">
        {/* Sidebar */}
        <aside className="hidden md:block w-64 shrink-0 border-r border-muted pt-16 pb-20 pr-8 sticky top-16 scroll-mt-16 h-[calc(100vh-4rem)] overflow-y-auto">
          <nav className="space-y-10 pl-4">
            <div>
              <h4 className="font-semibold mb-4 text-xs uppercase tracking-wider text-gray-900 dark:text-gray-100">Getting Started</h4>
              <ul className="space-y-1">
                <SidebarLink href="#installation">Installation & Setup</SidebarLink>
                <SidebarLink href="#hello-world">Your First App</SidebarLink>
              </ul>
            </div>
            <div>
              <h4 className="font-semibold mb-4 text-xs uppercase tracking-wider text-gray-900 dark:text-gray-100">Core Concepts</h4>
              <ul className="space-y-1">
                <SidebarLink href="#routing">Routing & Context</SidebarLink>
                <SidebarLink href="#controllers">Controllers & Grouping</SidebarLink>
                <SidebarLink href="#responses">Responses & JSON Parsing</SidebarLink>
                <SidebarLink href="#exception-handling">Exception Handling</SidebarLink>
              </ul>
            </div>
            <div>
              <h4 className="font-semibold mb-4 text-xs uppercase tracking-wider text-gray-900 dark:text-gray-100">Security & Middleware</h4>
              <ul className="space-y-1">
                <SidebarLink href="#middleware">Middleware & Auth</SidebarLink>
                <SidebarLink href="#cors-ratelimit">CORS & Rate Limiting</SidebarLink>
                <SidebarLink href="#tls-security">Native TLS (HTTPS)</SidebarLink>
              </ul>
            </div>
            <div>
              <h4 className="font-semibold mb-4 text-xs uppercase tracking-wider text-gray-900 dark:text-gray-100">Advanced Features</h4>
              <ul className="space-y-1">
                <SidebarLink href="#database">Async Database (HikariCP)</SidebarLink>
                <SidebarLink href="#static-files">Zero-Copy Static Files</SidebarLink>
                <SidebarLink href="#graceful-shutdown">Graceful Shutdown</SidebarLink>
                <SidebarLink href="#plugins">Plugin Architecture</SidebarLink>
                <SidebarLink href="#tuning">Concurrency & Thread Pool</SidebarLink>
              </ul>
            </div>
            <div>
              <h4 className="font-semibold mb-4 text-xs uppercase tracking-wider text-gray-900 dark:text-gray-100">Reference</h4>
              <ul className="space-y-1">
                <SidebarLink href="#configuration">Configuration Matrix</SidebarLink>
              </ul>
            </div>
          </nav>
        </aside>

        {/* Main docs content */}
        <main className="flex-1 px-6 md:px-20 py-16 max-w-[900px]">
          {/* Hero Section */}
          <div className="mb-16">
            <div className="inline-flex items-center rounded-full border border-muted bg-muted/50 px-3 py-1 text-xs font-medium text-gray-600 dark:text-gray-300 mb-6 transition-colors hover:bg-muted">
              Technical Documentation
            </div>
            <h1 className="text-4xl md:text-5xl font-bold tracking-tight mb-6 text-balance text-gray-900 dark:text-white">
              The NioFlow Stack
            </h1>
            <p className="text-lg text-gray-600 dark:text-gray-400 text-balance leading-relaxed">
              Official technical reference. Learn how the repository is structured, how to add custom business logic, scale the database, and configure the engine for production environments.
            </p>
          </div>

          <H2 id="installation">Installation & Setup</H2>
          <div className="prose prose-gray dark:prose-invert max-w-none text-gray-600 dark:text-gray-400 text-[15px] leading-relaxed">
            <p className="mb-4">
              Unlike large Java frameworks like Spring Boot, NioFlow does not employ reflection or classpath scanning. It provides an explicit programmatic model. You wire up your controllers, middleware, and exception handlers directly in code.
            </p>
            <div className="my-8 rounded-xl border-l-4 border-black dark:border-white bg-muted/30 p-6 shadow-sm">
              <p className="m-0 text-gray-900 dark:text-gray-100 font-medium tracking-tight">
                Hybrid Non-Blocking / Blocking Architecture
              </p>
              <p className="mt-2 mb-0">
                The framework uses Java's <code>java.nio.channels.Selector</code> to accept inbound connections asynchronously. Once a connection is established, the <code>SocketChannel</code> is handed off to a <code>ThreadPoolExecutor</code> equipped with a bounded <code>ArrayBlockingQueue</code>. This creates a predictable environment where concurrent requests are handled safely, with automatic rejections (HTTP 503) if the internal thread pool queue fills up.
              </p>
            </div>
          </div>
          <H2 id="hello-world">Your First NioFlow App</H2>
          <p className="text-gray-600 dark:text-gray-400 mb-6 text-[15px] leading-relaxed">
            Bootstrapping a NioFlow application requires instantiating the <code>NioFlowApp</code> class. From here, you use our fluent API to map HTTP verbs to handler lambdas.
          </p>

          <CodeBlock
            title="pom.xml"
            language="xml"
            code={`<dependencies>
    <dependency>
        <groupId>com.jhanvi857</groupId>
        <artifactId>nioflow-framework</artifactId>
        <version>1.0-SNAPSHOT</version>
    </dependency>
</dependencies>`}
          />

          <H2 id="hello-world">Your First NioFlow App</H2>
          <p className="text-gray-600 dark:text-gray-400 mb-6 text-[15px] leading-relaxed">
            Bootstrapping a NioFlow application requires instantiating the <code>NioFlowApp</code> class. From here, you use our fluent API to map HTTP verbs to handler lambdas.
          </p>

          <CodeBlock
            title="App.java"
            language="java"
            code={`import com.jhanvi857.nioflow.NioFlowApp;

public class App {
    public static void main(String[] args) {
        // 1. Initialize the app instance
        NioFlowApp app = new NioFlowApp();

        // 2. Define your routes (Express.js style)
        app.get("/", ctx -> ctx.send("Hello, World from NioFlow!"));
        
        app.get("/health", ctx -> {
             ctx.json("{ \\"status\\": \\"ok\\" }"); 
        });

        // 3. Start the non-blocking I/O Event Loop!
        app.listen(8080);
    }
}`}
          />

          <H2 id="routing">Routing & Path Parameters</H2>
          <p className="text-gray-600 dark:text-gray-400 mb-6 text-[15px] leading-relaxed">
            NioFlow utilizes basic regex processing for path routing. The most powerful element in building routes is the <code>HttpContext</code> object, which provides a unified, typed interface for extracting request data and mutating the HTTP response.
          </p>
          
          <H3>The HttpContext API</H3>
          <p className="text-gray-600 dark:text-gray-400 mb-4 text-[15px] leading-relaxed">
            Every handler lambda receives a <code>ctx</code> parameter. Here is a comprehensive example demonstrating everything you can do with a router context:
          </p>

          <CodeBlock
            title="ContextDemo.java"
            language="java"
            code={`app.post("/api/users/:userId/documents/*", ctx -> {
    // 1. Extract PATH parameters defined with ':'
    String userId = ctx.pathParam("userId");
    
    // 2. Access routing internals implicitly
    String fullPath = ctx.path();   // e.g., "/api/users/123/documents/report.pdf"
    String method = ctx.method();   // "POST"
    
    // 3. Extract Headers
    String authHeader = ctx.header("Authorization");
    
    // 4. Query string extraction (extracted from URI manually for now)
    String uri = ctx.getRequest().getUri();
    
    // ==========================================
    // Request Body Parsing
    // ==========================================
    
    // Option A: Raw String
    String rawBody = ctx.bodyAsString();
    
    // Option B: Native JSON Deserialization (uses Jackson internally)
    CreateDocRequest dto = ctx.body(CreateDocRequest.class);
    System.out.println("Extracted DTO title: " + dto.getTitle());
    
    // ==========================================
    // Response Mutation
    // ==========================================
    
    // Set custom response headers
    ctx.header("X-Engine", "NioFlow-v1");
    ctx.header("Cache-Control", "no-cache");
    
    // Send standard text
    if (dto == null) {
        ctx.status(400).send("Bad Request: Missing JSON body");
        return;
    }
    
    // Send serialized JSON Data with a custom HTTP Status (201 Created)
    ctx.status(HttpStatus.CREATED).json(Map.of(
        "success", true,
        "documentOwner", userId,
        "processedLen", rawBody.length()
    ));
});`}
          />
          <H2 id="responses">Responses & JSON Parsing</H2>
          <p className="text-gray-600 dark:text-gray-400 mb-6 text-[15px] leading-relaxed">
            By default, NioFlow uses <code>Jackson</code> to seamlessly integrate POJO parsing out-of-the-box. Handlers execute sequentially.
          </p>

          <CodeBlock
            title="JsonHandler.java"
            language="java"
            code={`app.post("/api/create", ctx -> {
    // 1. Native mapping to a Java Class Model
    UserPayload payload = ctx.body(UserPayload.class);
    
    // 2. Setting HTTP Status codes strictly
    ctx.status(HttpStatus.CREATED);
    
    // 3. Adding headers individually
    ctx.header("Cache-Control", "no-cache");
    
    // 4. Returning HashMaps implicitly serializes to JSON
    ctx.json(Map.of(
        "id", 101,
        "payloadTitle", payload.getTitle()
    ));
});`}
          />

          <H2 id="controllers">Controllers & Route Grouping</H2>
          <p className="text-gray-600 dark:text-gray-400 mb-6 text-[15px] leading-relaxed">
            Putting all routes in your <code>main()</code> method becomes unmaintainable quickly. NioFlow lets you modularize endpoints using the <code>app.group()</code> API and isolated Controller classes.
          </p>
          
          <CodeBlock
            title="TaskController.java"
            language="java"
            code={`public class TaskController {
    
    // Pass the route group prefix builder here
    public static void register(RouteGroup api) {
        api.get("/", ctx -> {
            ctx.json("List of all tasks from the DB");
        });
        
        api.post("/", ctx -> {
            ctx.status(201).json("Created a new task");
        });
    }
}`}
          />
          <CodeBlock
            title="App.java"
            language="java"
            code={`// Wiring up your Controller to a specific API prefix
app.group("/api/v1/tasks", api -> {
    TaskController.register(api);
});`}
          />

          <H2 id="middleware">Middleware & Authentication</H2>
          <p className="text-gray-600 dark:text-gray-400 mb-6 text-[15px] leading-relaxed">
            Every request travels through a strict Chain-of-Responsibility pipeline. NioFlow lets you apply <code>Middleware</code> globally across the whole app, or locally to specific route groups.
          </p>
          
          <CodeBlock
            title="App.java"
            language="java"
            code={`// 1. Global Middleware (Runs on EVERY request)
app.use(new LoggerMiddleware());
app.use(new CorsMiddleware("*"));
app.use(new RateLimitMiddleware(100, 60000)); // 100 reqs / min

// 2. Scoped Middleware System (e.g. Securing an API chunk)
app.group("/api/secure", secureApi -> {
    
    // This middleware ONLY runs before routes inside /api/secure/*
    secureApi.use(ctx -> {
        String token = ctx.header("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            ctx.status(401).json("Missing JWT Token");
            return false; // Returns 'false' to halt pipeline
        }
        
        // Custom logic to verify JWT here...
        ctx.setAttribute("userId", "12345"); // Pass data to controllers
        return true; // Pipeline continues
    });
    
    // This route is now protected!
    secureApi.get("/dashboard", ctx -> {
        String userId = (String) ctx.getAttribute("userId");
        ctx.json("Welcome user: " + userId);
    });
});`}
          />

          <H2 id="cors-ratelimit">CORS & Rate Limiting</H2>
          <p className="text-gray-600 dark:text-gray-400 mb-6 text-[15px] leading-relaxed">
            We provide pre-built middleware for critical networking constraints. Rate Limiting tracks IPs implicitly and rejects abusive traffic with an HTTP 429 status code.
          </p>

          <CodeBlock
            title="App.java"
            language="java"
            code={`// 1. Cross-Origin Resource Sharing
// Allows browsers from specific domains to make AJAX requests.
// You can pass an environment variable like origin="http://localhost:3000"
app.use(new CorsMiddleware("*")); 

// 2. Volumetric Rate Limiting
// Prevents Denial-of-Service by limiting an IP to [Max Requests] per [Timeout Ms]
// e.g., 100 requests per 60000ms (1 minute)
app.use(new RateLimitMiddleware(100, 60000));`}
          />

          <H2 id="exception-handling">Exception Handling</H2>
          <p className="text-gray-600 dark:text-gray-400 mb-6 text-[15px] leading-relaxed">
            Don't clutter your route logic with infinite try-catch blocks. NioFlow provides a centralized <code>GlobalExceptionHandler</code>. If any controller or middleware throws an exception, NioFlow catches it and applies your defined mapped response.
          </p>

          <CodeBlock
            title="App.java"
            language="java"
            code={`// Map a specific Exception Class to an HTTP response globally
app.exception(IllegalArgumentException.class, (e, ctx) -> {
    ctx.status(400).json(Map.of("error", e.getMessage()));
});

// Map standard Java Exceptions (Catch-all Fallback)
app.exception(Exception.class, (e, ctx) -> {
    e.printStackTrace(); // Log internally
    ctx.status(500).json(Map.of("error", "Internal Server Error"));
});

// Now, your controllers can throw freely!
app.get("/error-prone", ctx -> {
    throw new IllegalArgumentException("Invalid ID format!"); 
    // ^ Will return 400 Bad Request JSON automatically
});`}
          />

          <H2 id="tls-security">Native TLS (HTTPS)</H2>
          <p className="text-gray-600 dark:text-gray-400 mb-6 text-[15px] leading-relaxed">
            NioFlow natively terminates TLS for raw secure connections using Java's built-in <code>SSLContext</code>, avoiding the necessity of external proxies like NGINX or Caddy. During the NIO Selector event loop, the raw <code>SocketChannel</code> is upgraded utilizing a highly secure <code>SSLSocketFactory</code> handoff before your middleware triggers.
          </p>
          
          <CodeBlock
            title="App.java"
            language="java"
            code={`// Provide a keystore and pass to enable native HTTPS on port 443
app.listenSecure(443, "keystore.jks", "my-secure-password");`}
          />

          <H2 id="database">Async Database Offload</H2>
          <p className="text-gray-600 dark:text-gray-400 mb-6 text-[15px] leading-relaxed">
            JDBC is inherently synchronous. To prevent blocking the main NioFlow worker threads, we strongly recommend offloading database queries to a dedicated secondary executor using <code>CompletableFuture</code>. Combine this with HikariCP for aggressive connection pooling.
          </p>

          <CodeBlock
            title="Database Integration (CompletableFuture)"
            language="java"
            code={`// 1. Initialize DB Executor
ExecutorService dbExecutor = Executors.newFixedThreadPool(10); // Match DB Pool Size

// 2. Wrap Synchronous JDBC in CompletableFuture
public CompletableFuture<List<Map<String, Object>>> fetchTasksAsync(HikariDataSource ds) {
    return CompletableFuture.supplyAsync(() -> {
        List<Map<String, Object>> tasks = new ArrayList<>();
        try (Connection conn = ds.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM tasks");
             ResultSet rs = stmt.executeQuery()) {
             
             // Process ResultSet
             while (rs.next()) {
                 tasks.add(Map.of("id", rs.getInt("id"), "title", rs.getString("title")));
             }
             return tasks;
        } catch (SQLException e) {
             throw new CompletionException("DB Error", e); // Caught by global ErrorHandler
        }
    }, dbExecutor);
}

// 3. Controller execution
app.get("/api/tasks", ctx -> {
    // Execution waits on the Future, ensuring ThreadPool safety limits are maintained
    List<Map<String, Object>> result = fetchTasksAsync(myDataSource).join(); 
    ctx.status(200).json(result);
});`}
          />

          <H2 id="static-files">Serving Static Files</H2>
          <p className="text-gray-600 dark:text-gray-400 mb-6 text-[15px] leading-relaxed">
            Need to host a React or frontend framework build? NioFlow utilizes advanced <strong>Zero-Copy (DMA)</strong> algorithms via <code>FileChannel.transferTo()</code> to serve static assets at immense speeds, completely bypassing user-space memory buffers.
          </p>
          
          <CodeBlock
            title="App.java"
            language="java"
            code={`NioFlowApp app = new NioFlowApp();

// Tell NioFlow to serve files from your "public" repository
// e.g. /public/index.html -> served at http://localhost:8080/index.html
app.register(new StaticFilesPlugin("src/main/resources/public"));

app.listen(8080);`}
          />

          <H2 id="graceful-shutdown">Graceful Shutdown</H2>
          <p className="text-gray-600 dark:text-gray-400 mb-6 text-[15px] leading-relaxed">
            To prevent dropping critical requests during container rollouts, NioFlow includes mechanical support for graceful terminations. Calling <code>drainAndStop()</code> stops the Selector event loop from accepting new socket streams, then safely awaits active I/O threads to finalize their outputs.
          </p>

          <CodeBlock
            title="App.java"
            language="java"
            code={`Runtime.getRuntime().addShutdownHook(new Thread(() -> {
    // Blocks for up to 30 seconds for active network/DB requests to finish cleanly
    app.drainAndStop(30, TimeUnit.SECONDS);
    System.out.println("NioFlow Framework shutdown complete.");
}));`}
          />

          <H2 id="plugins">Plugin Architecture</H2>
          <p className="text-gray-600 dark:text-gray-400 mb-6 text-[15px] leading-relaxed">
            The NioFlow ecosystem allows you to write custom modular logic that hooks directly into the <code>NioFlowApp</code> instance on boot. You implement the <code>NioFlowPlugin</code> interface.
          </p>

          <CodeBlock
            title="MetricsPlugin.java"
            language="java"
            code={`public class SampleMetricsPlugin implements NioFlowPlugin {
    @Override
    public void onRegister(NioFlowApp app) {
        // Register standard internal endpoints automatically
        app.get("/_metrics", ctx -> {
            ctx.status(200).json(Map.of("activeThreads", Thread.activeCount()));
        });
        
        // Push a global middleware metric tracker
        app.use(new MetricsMiddleware());
    }
}

// Inside Main Class:
app.register(new SampleMetricsPlugin());`}
          />

          <H2 id="tuning">Concurrency & Thread Pool</H2>
          <p className="text-gray-600 dark:text-gray-400 mb-6 text-[15px] leading-relaxed">
            The Java NIO Selector manages connections, but actual request parsing runs in a bounded thread pool. If the <code>ThreadPoolExecutor</code> reaches its max core limit, concurrent requests pile up in the <code>ArrayBlockingQueue</code>. If the Queue fills up completely, NioFlow safely drops the connection returning an <code>HTTP 503 Service Unavailable</code> to defensively protect heap memory.
          </p>

          <CodeBlock
            title="Environment Variables"
            language="bash"
            code={`# Define Max Concurrent Worker Threads (Default: 10)
export NIOFLOW_THREADS=25

# Define Max Backlog in the Worker Queue before 503 Rejection (Default: 100)
export NIOFLOW_QUEUE_CAPACITY=500

# Define Socket Timeout (Default: 15000ms)
export NIOFLOW_SOCKET_TIMEOUT_MS=5000`}
          />



          <H2 id="configuration">Configuration Matrix</H2>
          <p className="text-gray-600 dark:text-gray-400 mb-6 text-[15px] leading-relaxed">
            NioFlow can be entirely configured via the runtime environment, ensuring robust adherence to 12-factor application principles in modern containerized deployments.
          </p>

          <div className="overflow-hidden rounded-xl border border-muted bg-card shadow-sm mb-20">
            <div className="overflow-x-auto">
              <table className="w-full text-left text-sm whitespace-nowrap">
                <thead>
                  <tr className="border-b border-muted bg-muted/40 text-xs text-gray-500 uppercase tracking-wider">
                    <th className="px-6 py-4 font-medium">Component / Target</th>
                    <th className="px-6 py-4 font-medium text-right">Environment Variable</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-muted/50 text-[13px]">
                  {configRows.map((row, idx) => (
                    <tr key={`${row.jvmProperty}-${idx}`} className="transition-colors hover:bg-muted/20">
                      <td className="px-6 py-4 text-gray-700 dark:text-gray-300">{row.purpose}</td>
                      <td className="px-6 py-4 font-mono text-right text-gray-500 dark:text-gray-400">
                        <code className="bg-muted px-2 py-1 rounded border border-muted/50 text-[11px]">{row.envVar}</code>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        </main>
      </div>
      <Footer />
    </div>
  );
}
