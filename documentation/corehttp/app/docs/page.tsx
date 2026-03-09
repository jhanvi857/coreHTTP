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
    if (/^(Router|HttpServer|Auth|Role|OrderController|String|HttpStatus|System|out|req|res)$/.test(t)) return "text-[#b392f0]";
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
              <h4 className="font-semibold mb-4 text-xs uppercase tracking-wider text-gray-900 dark:text-gray-100">Overview</h4>
              <ul className="space-y-1">
                <SidebarLink href="#philosophy">Architecture Philosophy</SidebarLink>
                <SidebarLink href="#quick-start">Quick Start</SidebarLink>
                <SidebarLink href="#project-structure">Project Structure</SidebarLink>
              </ul>
            </div>
            <div>
              <h4 className="font-semibold mb-4 text-xs uppercase tracking-wider text-gray-900 dark:text-gray-100">Deep Dive</h4>
              <ul className="space-y-1">
                <SidebarLink href="#routing">Routing & Controllers</SidebarLink>
                <SidebarLink href="#database">Database & Schema</SidebarLink>
                <SidebarLink href="#auth-middleware">Auth & Middleware</SidebarLink>
              </ul>
            </div>
            <div>
              <h4 className="font-semibold mb-4 text-xs uppercase tracking-wider text-gray-900 dark:text-gray-100">Reference</h4>
              <ul className="space-y-1">
                <SidebarLink href="#api-endpoints">API Endpoints</SidebarLink>
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
              The coreHTTP Stack
            </h1>
            <p className="text-lg text-gray-600 dark:text-gray-400 text-balance leading-relaxed">
              Official technical reference. Learn how the repository is structured, how to add custom business logic, scale the database, and configure the engine for production environments.
            </p>
          </div>

          <H2 id="philosophy">Architecture Philosophy</H2>
          <div className="prose prose-gray dark:prose-invert max-w-none text-gray-600 dark:text-gray-400 text-[15px] leading-relaxed">
            <p className="mb-4">
              Most Java projects rely heavily on bloated enterprise frameworks or embedded servlet containers. While powerful, these frameworks often mask the underlying intricacies of network programming, concurrency, and thread management.
            </p>
            <div className="my-8 rounded-xl border-l-4 border-black dark:border-white bg-muted/30 p-6 shadow-sm">
              <p className="m-0 text-gray-900 dark:text-gray-100 font-medium tracking-tight">
                coreHTTP is built fundamentally differently.
              </p>
              <p className="mt-2 mb-0">
                It strips away the magic, relying strictly on the JVM's <code>java.nio</code> module. By implementing a custom, single-threaded Event Loop using multiplexed Selectors, the engine gracefully handles thousands of concurrent socket connections without the heavy memory footprint of traditional thread-per-request models.
              </p>
            </div>
          </div>

          <H2 id="quick-start">Quick Start</H2>
          <p className="text-gray-600 dark:text-gray-400 mb-6 text-[15px] leading-relaxed">
            Bootstrapping the framework in your local environment is straightforward. You can run it natively via PowerShell or containerize the entire stack, including the PostgreSQL database, using Docker.
          </p>
          <div className="grid md:grid-cols-2 gap-6 mb-8">
            <div className="flex flex-col">
              <div className="flex items-center justify-between mb-2 px-1">
                <h4 className="font-semibold text-sm text-gray-900 dark:text-white">Native Runtime</h4>
                <span className="text-xs text-gray-400">Windows</span>
              </div>
              <CodeBlock code=".\scripts\run.ps1" language="powershell" title="powershell" />
            </div>
            <div className="flex flex-col">
              <div className="flex items-center justify-between mb-2 px-1">
                <h4 className="font-semibold text-sm text-gray-900 dark:text-white">Docker Deployment</h4>
                <span className="text-xs text-gray-400">Universal</span>
              </div>
              <CodeBlock code="docker-compose up -d --build" language="bash" title="bash" />
            </div>
          </div>

          <H2 id="project-structure">Project Structure</H2>
          <p className="text-gray-600 dark:text-gray-400 mb-6 text-[15px] leading-relaxed">
            coreHTTP is strictly organized into decoupled layers, separating protocol transport from business logic. Understanding this structure is key to modifying the application to suit your needs:
          </p>

          <div className="space-y-4 mb-10">
            {[
              { path: '/server', name: 'NIO Transport Layer', desc: 'Modifying core socket handling, selectors, and zero-copy behaviors.' },
              { path: '/app/controller', name: 'Controllers', desc: 'Where you write application endpoints and register your business logic.' },
              { path: '/app/repository', name: 'Persistence', desc: 'JDBC operations and database access objects using HikariCP.' },
              { path: '/init-db.sql', name: 'Database Schema', desc: 'The raw SQL executed by the Postgres container at startup.' }
            ].map((item, i) => (
              <div key={i} className="flex flex-col sm:flex-row sm:items-baseline gap-2 sm:gap-4 p-4 rounded-xl border border-muted bg-muted/20 hover:bg-muted/40 transition-colors">
                <code className="text-xs font-mono text-gray-800 dark:text-gray-200 bg-muted px-2 py-1 rounded w-fit sm:w-48 shrink-0">
                  {item.path}
                </code>
                <div className="text-[14px]">
                  <span className="font-semibold text-gray-900 dark:text-white mr-2">{item.name}:</span>
                  <span className="text-gray-600 dark:text-gray-400">{item.desc}</span>
                </div>
              </div>
            ))}
          </div>

          <H2 id="routing">Routing & Controllers</H2>
          <p className="text-gray-600 dark:text-gray-400 mb-4 text-[15px] leading-relaxed">
            The coreHTTP router uses a declarative, map-based structure to resolve paths to handler lambdas. To register custom endpoints, create a new controller class in the <code className="text-xs bg-muted px-1.5 py-0.5 rounded">app/controller</code> directory and inject the Router singleton.
          </p>

          <CodeBlock
            title="UserController.java"
            language="java"
            code={`public class UserController {
    public void registerRoutes(Router router) {
        // Example: Registering a new POST endpoint
        router.post("/api/users", (req, res) -> {
            String payload = new String(req.getBody());
            
            // Insert custom business logic here
            System.out.println("Processing user: " + payload);
            
            res.setStatusCode(HttpStatus.CREATED);
            res.setBody("{\\"status\\":\\"success\\"}".getBytes());
            res.addHeader("Content-Type", "application/json");
        });
    }
}`}
          />
          <p className="text-gray-500 dark:text-gray-500 mt-4 text-[13px]">
            * After creating a controller, ensure you declare and attach it securely in the main <code className="text-[12px] bg-muted px-1 py-0.5 rounded text-gray-700 dark:text-gray-300">HttpServer.java</code> bootstrapper.
          </p>

          <H2 id="database">Database & Schema</H2>
          <p className="text-gray-600 dark:text-gray-400 mb-6 text-[15px] leading-relaxed">
            The framework utilizes robust JDBC integration with <strong>HikariCP</strong> connection pooling natively out-of-the-box. All database models are strictly typed and queried using standard prepared statements.
          </p>

          <H3>Customizing Database Tables</H3>
          <p className="text-gray-600 dark:text-gray-400 mb-4 text-[15px] leading-relaxed">
            Database definitions are strictly managed by Docker initializing the PostgreSQL volume. If you need to add custom schema, modify the root <code className="text-xs bg-muted px-1.5 py-0.5 rounded">init-db.sql</code> script.
          </p>

          <CodeBlock
            title="init-db.sql"
            language="sql"
            code={`-- Example: Add customized application domains inside init-db.sql
CREATE TABLE IF NOT EXISTS orders (
    id SERIAL PRIMARY KEY,
    user_id INT NOT NULL,
    total DECIMAL(10,2) NOT NULL,
    status VARCHAR(50) DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);`}
          />

          <H2 id="auth-middleware">Auth & Middleware Architecture</H2>
          <p className="text-gray-600 dark:text-gray-400 mb-8 text-[15px] leading-relaxed">
            We employ a true Chain-of-Responsibility pattern for cross-cutting logic. All incoming data passes through the <code className="text-xs bg-muted px-1.5 py-0.5 rounded">/middleware</code> package before hitting your controllers.
          </p>

          <div className="grid gap-6 mb-12">
            <div className="relative overflow-hidden rounded-2xl border border-muted bg-card p-8 shadow-sm">
              <div className="absolute top-0 left-0 w-1 h-full bg-black dark:bg-white" />
              <h5 className="font-bold text-gray-900 dark:text-white text-base mb-3">AuthMiddleware.java</h5>
              <p className="text-[14px] text-gray-600 dark:text-gray-400 leading-relaxed">
                Extracts and verifies <code>Authorization: Bearer</code> JSON Web Tokens using strict BCrypt verification. Rejects illicit requests immediately, sparing CPU cycles further down the chain.
              </p>
            </div>
            <div className="relative overflow-hidden rounded-2xl border border-muted bg-card p-8 shadow-sm">
              <div className="absolute top-0 left-0 w-1 h-full bg-gray-300 dark:bg-gray-700" />
              <h5 className="font-bold text-gray-900 dark:text-white text-base mb-3">Metrics & Logger Middleware</h5>
              <p className="text-[14px] text-gray-600 dark:text-gray-400 leading-relaxed">
                Collects endpoint latencies and handles structured logging. This isolates monitoring concerns, providing endpoints like <code>/metrics</code> the data needed for Prometheus scraping without polluting business controllers.
              </p>
            </div>
          </div>

          <H2 id="api-endpoints">API Endpoints Overview</H2>
          <p className="text-gray-600 dark:text-gray-400 mb-6 text-[15px] leading-relaxed">
            The following table describes the standard endpoints shipped with the framework out-of-the-box, covering system health, security, and the sample tasks schema.
          </p>

          <div className="overflow-hidden rounded-xl border border-muted bg-card shadow-sm my-8">
            <div className="overflow-x-auto">
              <table className="w-full text-left text-sm whitespace-nowrap">
                <thead>
                  <tr className="border-b border-muted bg-muted/40 text-xs text-gray-500 uppercase tracking-wider">
                    <th className="px-6 py-4 font-medium">Method</th>
                    <th className="px-6 py-4 font-medium">Path</th>
                    <th className="px-6 py-4 font-medium">Purpose</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-muted/50 text-[13px]">
                  {endpoints.map((endpoint, idx) => (
                    <tr key={`${endpoint.method}-${endpoint.path}-${idx}`} className="transition-colors hover:bg-muted/20">
                      <td className="px-6 py-4 whitespace-nowrap">
                        <span className={`inline-flex items-center justify-center min-w-[50px] rounded px-2 py-1 text-[10px] font-bold tracking-widest ${endpoint.method === "GET"
                          ? "bg-slate-100 text-slate-700 dark:bg-slate-800 dark:text-slate-300"
                          : endpoint.method === "POST"
                            ? "bg-zinc-800 text-white dark:bg-zinc-200 dark:text-black"
                            : "bg-gray-100 text-gray-600 dark:bg-gray-800 dark:text-gray-400"
                          }`}>
                          {endpoint.method}
                        </span>
                      </td>
                      <td className="px-6 py-4 font-mono text-gray-900 dark:text-gray-200">{endpoint.path}</td>
                      <td className="px-6 py-4 text-gray-500 dark:text-gray-400">{endpoint.purpose}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>

          <H2 id="configuration">Configuration Matrix</H2>
          <p className="text-gray-600 dark:text-gray-400 mb-6 text-[15px] leading-relaxed">
            coreHTTP can be entirely configured via the runtime environment, ensuring robust adherence to 12-factor application principles in modern containerized deployments.
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
