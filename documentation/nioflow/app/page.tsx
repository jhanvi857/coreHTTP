import Link from "next/link";
import CodeDemo from "./components/CodeDemo";
import Footer from "./components/Footer";
import Navbar from "./components/Navbar";
import DependencyTabs from "./components/DependencyTabs";
import BenchmarkStats from "./components/BenchmarkStats";



export default function Home() {
  return (
    <div className="app-shell bg-[#050505] text-white selection:bg-blue-500/30">
      <Navbar />

      <main className="flex-1 w-full flex flex-col items-center">
        {/* HERO SECTION */}
        <section className="relative w-full pt-24 pb-24 flex flex-col items-center justify-center border-b border-[#1a1a1a] overflow-hidden bg-black/80">
          {/* Subtle Grid Background */}
          {/* <div className="absolute inset-0 bg-[linear-gradient(rgba(255,255,255,0.03)_1px,transparent_1px),linear-gradient(90deg,rgba(255,255,255,0.03)_1px,transparent_1px)] bg-[size:60px_60px] pointer-events-none" /> */}

          <div className="relative z-10 max-w-6xl mx-auto px-6 w-full flex flex-col lg:flex-row items-center gap-16">

            <div className="flex-1 flex flex-col items-start text-left">
              <div className="flex flex-wrap items-center gap-4 mb-8">
                <div className="inline-flex items-center gap-2 px-3 py-1.5 text-[13px] font-medium border border-[#333] rounded-full bg-[#111]">
                  <span className="flex h-2 w-2 rounded-full bg-blue-500 shadow-[0_0_8px_rgba(59,130,246,0.8)]" />
                  <span className="text-gray-300">Java NIO Systems Engineering</span>
                </div>
                <div className="flex items-center gap-4 text-[12px] font-mono text-gray-500 border-l border-[#333] pl-4">
                  <span className="flex items-center gap-1.5"><span className="text-blue-400">501</span> req/s</span>
                  <span className="flex items-center gap-1.5"><span className="text-green-400">1.5ms</span> p50</span>
                  <span className="flex items-center gap-1.5"><span className="text-yellow-400">99.8%</span> success</span>
                </div>
              </div>


              <h1 className="text-4xl lg:text-5xl font-bold tracking-tight text-white leading-tight mb-6">
                NioFlow: A Configurable Java <span className="text-transparent bg-clip-text bg-gradient-to-r from-gray-200 to-gray-600">Micro-Framework.</span>
              </h1>

              <p className="text-lg text-gray-400 mb-10 leading-relaxed max-w-xl">
                NioFlow is a minimalistic Java HTTP framework focusing on explicit programmatic configuration. It utilizes a hybrid architecture: NIO Selector for connection acceptance and a bounded thread pool for blocking request processing.
              </p>

              {/* <div className="bg-[#111] border border-[#333] rounded-lg p-4 mb-10 max-w-xl">
                <h3 className="text-sm font-bold text-white mb-1">Why not Spring Boot?</h3>
                <p className="text-sm text-gray-400">
                  NioFlow is not a Spring Boot replacement. It is built for developers who want to understand what a framework does before using one that hides it.
                </p>
              </div> */}

              <div className="flex flex-col sm:flex-row gap-4 w-full sm:w-auto">
                <Link href="/docs" className="inline-flex justify-center items-center h-12 px-8 text-[15px] font-medium bg-white text-black hover:bg-gray-200 transition-colors rounded-lg shadow-md">
                  Read Architecture Docs
                </Link>
                <Link href="https://github.com/jhanvi857/coreHTTP" target="_blank" className="inline-flex justify-center items-center h-12 px-8 text-[15px] font-medium bg-transparent text-white border border-[#333] hover:bg-[#111] transition-colors rounded-lg">
                  View Source Code
                </Link>
              </div>
            </div>

            {/* Mac Terminal Graphic */}
            <div className="flex-1 w-full max-w-lg lg:max-w-none">
              <div className="rounded-xl border border-[#333] bg-[#0e0e11] overflow-hidden shadow-2xl relative">
                <div className="flex items-center px-4 py-3 border-b border-[#222] bg-[#1a1a1e] relative">
                  <div className="flex space-x-2 absolute left-4">
                    <div className="w-3 h-3 rounded-full bg-[#ff5f56]"></div>
                    <div className="w-3 h-3 rounded-full bg-[#ffbd2e]"></div>
                    <div className="w-3 h-3 rounded-full bg-[#27c93f]"></div>
                  </div>
                  <div className="w-full text-center">
                    <span className="text-xs font-mono text-gray-400">nioflow_server — bash</span>
                  </div>
                </div>
                <div className="p-6 font-mono text-[13px] text-gray-300 leading-relaxed h-[320px] overflow-hidden relative">
                  <div className="absolute inset-0 bg-gradient-to-b from-transparent via-transparent to-[#0e0e11] bottom-0 h-full w-full z-10 pointer-events-none" />
                  <p><span className="text-green-400">$</span> npm install -g @jhanvi857/nioflow-cli</p>
                  <p className="text-gray-500 mt-2">[INFO] nioflow: Linked global binary successfully.</p>
                  <p><span className="text-green-400">$</span> nioflow dev</p>
                  <p className="text-gray-500 mt-2">[INFO] Watcher: Monitoring src/main/java...</p>
                  <p className="text-gray-500">[INFO] Bootstrap: Starting NioFlow v1.4.0</p>
                  <p className="text-gray-500">[INFO] NIO: ServerSocketChannel on 0.0.0.0:8080</p>
                  <p className="text-blue-400 mb-4">[READY] Application live with Hot Reload enabled.</p>
                  <p className="mt-4"><span className="text-green-400">$</span> curl http://localhost:8080/metrics</p>
                  <p className="text-yellow-400 mt-2">HTTP/1.1 200 OK</p>
                  <p className="text-gray-300 mt-2">{`{ "req_total": 1205, "p95_ms": 47.7 }`}</p>
                </div>
              </div>
            </div>

          </div>
        </section>

        {/* QUICK INSTALL SECTION */}
        <section className="w-full py-16 border-b border-[#1a1a1a] bg-[#080808]">
          <div className="max-w-[1200px] mx-auto px-6">
            <div className="flex flex-col md:flex-row items-center gap-12">
              <div className="flex-1">
                <h2 className="text-3xl font-bold mb-4 text-white">Fast Track Installation</h2>
                <p className="text-gray-400 mb-6">
                  Get up and running in seconds. The NioFlow CLI manages your Java environment, dependencies, and project scaffolding so you can focus on building routes.
                </p>
                <ul className="space-y-3 text-sm text-gray-500 mb-8">
                  <li className="flex items-center gap-2">
                    <span className="text-blue-500 font-bold">✓</span> No manual JAR downloads required
                  </li>
                  <li className="flex items-center gap-2">
                    <span className="text-blue-500 font-bold">✓</span> Automatic Maven Wrapper integration
                  </li>
                  <li className="flex items-center gap-2">
                    <span className="text-blue-500 font-bold">✓</span> Cross-platform support (Windows, macOS, Linux)
                  </li>
                </ul>
              </div>
              <div className="flex-1 w-full max-w-lg">
                <div className="bg-black border border-[#333] rounded-xl p-6 font-mono text-[14px]">
                  <div className="flex justify-between items-center mb-4 border-b border-[#222] pb-2">
                    <span className="text-gray-500 text-xs uppercase tracking-widest">Global Setup</span>
                    <span className="text-blue-400 text-xs">v1.4.0</span>
                  </div>
                  <p className="text-blue-300"># Install the CLI</p>
                  <p className="text-white mb-4">npm install -g @jhanvi857/nioflow-cli</p>
                  <p className="text-blue-300"># Scaffold & Start</p>
                  <p className="text-white">nioflow new my-app</p>
                  <p className="text-white">cd my-app</p>
                  <p className="text-white">nioflow dev</p>
                </div>
              </div>
            </div>
          </div>
        </section>

        {/* ADVANCED FEATURES SHOWCASE */}
        <section className="w-full py-24 bg-black">
          <div className="max-w-[1200px] mx-auto px-6">
            <div className="text-center mb-16">
              <h2 className="text-3xl md:text-5xl font-bold text-white mb-4">Production Resilience Pack</h2>
              <p className="text-gray-400 max-w-2xl mx-auto">
                Stop worrying about infrastructure. NioFlow ships with native middleware for resilience and observability that you normally spend weeks configuring.
              </p>
            </div>

            <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-6">
              {/* Chaos Middleware */}
              <div className="p-6 rounded-xl border border-[#222] bg-[#0e0e0e] hover:border-blue-500/50 transition-all group">
                <div className="w-10 h-10 rounded-lg bg-[#1a1a1a] border border-[#333] flex items-center justify-center font-mono text-xs text-gray-400 mb-6 transition-transform group-hover:scale-110">
                  CHS
                </div>
                <h3 className="text-lg font-bold text-white mb-2">Chaos Injection</h3>
                <p className="text-sm text-gray-500 mb-4">Validate frontend resilience by injecting latency and errors into your routes.</p>
                <div className="bg-black p-3 rounded font-mono text-[11px] text-blue-300/80">
                  {`app.use(new ChaosMiddleware()\n   .latency(200, 0.1));`}
                </div>
              </div>

              {/* Circuit Breaker */}
              <div className="p-6 rounded-xl border border-[#222] bg-[#0e0e0e] hover:border-blue-500/50 transition-all group">
                <div className="w-10 h-10 rounded-lg bg-[#1a1a1a] border border-[#333] flex items-center justify-center font-mono text-xs text-gray-400 mb-6 transition-transform group-hover:scale-110">
                  CBR
                </div>
                <h3 className="text-lg font-bold text-white mb-2">Circuit Breaker</h3>
                <p className="text-sm text-gray-500 mb-4">Fast-fail when downstream services are struggling to prevent cascading outages.</p>
                <div className="bg-black p-3 rounded font-mono text-[11px] text-blue-300/80">
                  {`group.use(new CircuitBreaker()\n   .threshold(0.5));`}
                </div>
              </div>

              {/* Request Replay */}
              <div className="p-6 rounded-xl border border-[#222] bg-[#0e0e0e] hover:border-blue-500/50 transition-all group">
                <div className="w-10 h-10 rounded-lg bg-[#1a1a1a] border border-[#333] flex items-center justify-center font-mono text-xs text-gray-400 mb-6 transition-transform group-hover:scale-110">
                  RPL
                </div>
                <h3 className="text-lg font-bold text-white mb-2">Request Replay</h3>
                <p className="text-sm text-gray-500 mb-4">Record and replay production-like requests locally for rapid debugging.</p>
                <div className="bg-black p-3 rounded font-mono text-[11px] text-blue-300/80">
                  {`app.enableReplay(50);`}
                </div>
              </div>
            </div>

            <div className="mt-12 text-center">
              <p className="inline-flex items-center gap-2 px-4 py-2 rounded-full bg-blue-500/10 text-blue-400 text-sm border border-blue-500/20">
                <svg className="w-4 h-4" fill="currentColor" viewBox="0 0 20 20"><path fillRule="evenodd" d="M2.166 4.999A11.954 11.954 0 0010 1.944 11.954 11.954 0 0017.834 5c.11.65.166 1.32.166 2.001 0 5.225-3.34 9.67-8 11.317C5.34 16.67 2 12.225 2 7c0-.682.057-1.35.166-2.001zm11.541 3.708a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clipRule="evenodd"></path></svg>
                Audited for CVEs post-release. CRLF injection, XFF spoofing, JWT gaps, and HTTP request smuggling patched in v1.4.0.
              </p>
            </div>
          </div>
        </section>

        {/* CORE MECHANICS SECTION */}
        <section className="w-full py-24 md:py-32 border-b border-[#1a1a1a] bg-gradient-to-r bg-[#050505]">
          <div className="max-w-[1200px] mx-auto px-6">
            <div className="mb-20 max-w-3xl">
              <h2 className="text-3xl md:text-5xl font-bold tracking-tight mb-6 text-white text-balance">
                Under the Hood: System Mechanics
              </h2>
              <p className="text-gray-400 text-lg md:text-xl leading-relaxed">
                Built on Java NIO and robust architectural patterns, NioFlow provides an incredibly fast, non-blocking foundation for building scalable web applications and RESTful APIs.
              </p>
            </div>

            <div className="grid md:grid-cols-3 gap-8">


              {/* Feature 2 */}
              <div className="p-8 rounded-2xl border border-[#222] bg-[#111] hover:bg-[#151515] transition-colors group">
                <div className="w-10 h-10 rounded-lg bg-[#1a1a1a] border border-[#333] flex items-center justify-center font-mono text-xs text-gray-400 mb-6 transition-transform group-hover:scale-110">
                  NIO
                </div>
                <h3 className="text-xl font-bold mb-3 text-white">Hybrid NIO Architecture</h3>
                <p className="text-gray-400 leading-relaxed text-[15px]">
                  Built on Java's <code className="text-gray-300 bg-[#222] px-1.5 py-0.5 rounded text-[13px]">java.nio</code>. Connection tracking and event loops use efficient Selectors, while request processing is handled by a bounded worker pool.
                </p>
              </div>

              {/* Feature 2 */}
              <div className="p-8 rounded-2xl border border-[#222] bg-[#111] hover:bg-[#151515] transition-colors group">
                <div className="w-10 h-10 rounded-lg bg-[#1a1a1a] border border-[#333] flex items-center justify-center font-mono text-xs text-gray-400 mb-6 transition-transform group-hover:scale-110">
                  TLS
                </div>
                <h3 className="text-xl font-bold mb-3 text-white">Native HTTPS Security</h3>
                <p className="text-gray-400 leading-relaxed text-[15px]">
                  Direct <code>SSLContext</code> integration allows raw <code>SocketChannel</code> handoffs to <code>SSLSocketFactory</code>, dropping the requirement for Nginx proxies entirely for HTTPS.
                </p>
              </div>

              {/* Feature 3 */}
              <div className="p-8 rounded-2xl border border-[#222] bg-[#111] hover:bg-[#151515] transition-colors group">
                <div className="w-10 h-10 rounded-lg bg-[#1a1a1a] border border-[#333] flex items-center justify-center font-mono text-xs text-gray-400 mb-6 transition-transform group-hover:scale-110">
                  DMA
                </div>
                <h3 className="text-xl font-bold mb-3 text-white">Zero-Copy Memory</h3>
                <p className="text-gray-400 leading-relaxed text-[15px]">
                  Hardware-accelerated Direct Memory Access via <code className="text-gray-300 bg-[#222] px-1.5 py-0.5 rounded text-[13px]">FileChannel.transferTo()</code>. Static files hit the socket without crossing into JVM user-space memory.
                </p>
              </div>

              {/* Feature 4 */}
              <div className="p-8 rounded-2xl border border-[#222] bg-[#111] hover:bg-[#151515] transition-colors group">
                <div className="w-10 h-10 rounded-lg bg-[#1a1a1a] border border-[#333] flex items-center justify-center font-mono text-xs text-gray-400 mb-6 transition-transform group-hover:scale-110">
                  DB
                </div>
                <h3 className="text-xl font-bold mb-3 text-white">Async Database Offload</h3>
                <p className="text-gray-400 leading-relaxed text-[15px]">
                  Prevent worker thread blocking using <code>CompletableFuture</code>. Operations hit HikariCP and PostgreSQL on a dedicated secondary loop, ensuring maximum throughput.
                </p>
              </div>



              {/* Feature 9 */}
              <div className="p-8 rounded-2xl border border-[#222] bg-[#111] hover:bg-[#151515] transition-colors group">
                <div className="w-10 h-10 rounded-lg bg-[#1a1a1a] border border-[#333] flex items-center justify-center font-mono text-xs text-gray-400 mb-6 transition-transform group-hover:scale-110">
                  HED
                </div>
                <h3 className="text-xl font-bold mb-3 text-white">Request Hedging</h3>
                <p className="text-gray-400 leading-relaxed text-[15px]">
                  Native tail-latency reduction. Automatically fire backup requests when primary executions cross latency thresholds to keep p99s consistently low.
                </p>
              </div>

              {/* Feature 6 */}
              <div className="p-8 rounded-2xl border border-[#222] bg-[#111] hover:bg-[#151515] transition-colors group">
                <div className="w-10 h-10 rounded-lg bg-[#1a1a1a] border border-[#333] flex items-center justify-center font-mono text-xs text-gray-400 mb-6 transition-transform group-hover:scale-110">
                  OTEL
                </div>
                <h3 className="text-xl font-bold mb-3 text-white">Observability Pack</h3>
                <p className="text-gray-400 leading-relaxed text-[15px]">
                  Native OpenTelemetry tracing, Prometheus metrics, and structured JSON logging. Get full visibility into your distributed system with zero external agents.
                </p>
              </div>
            </div>
          </div>
        </section>

        {/* CODE DEMO SECTION */}
        <section className="w-full bg-black/80 border-b border-[#1a1a1a] py-24 md:py-32 overflow-hidden">
          <div className="max-w-[1200px] mx-auto px-6 mb-16">
            <div className="max-w-2xl">
              <h2 className="text-3xl md:text-5xl font-bold tracking-tight mb-6 text-white">
                Clean Controller Abstraction
              </h2>
              <p className="text-gray-400 text-lg md:text-xl leading-relaxed">
                While the engine is complex at the socket layer, registering business logic remains declarative and heavily typed. Look at how easy it is to spin up new HTTP endpoints.
              </p>
            </div>
          </div>
          <div className="max-w-[1200px] mx-auto px-6">
            <CodeDemo />
          </div>
        </section>

        {/* THE CTA SECTION */}
        <section className="w-full bg-[#0a0a0a] text-white py-24 md:py-32">
          <div className="max-w-[1200px] mx-auto px-6 grid md:grid-cols-2 gap-16 items-center">

            {/* Left Content Column */}
            <div className="flex flex-col">
              <div className="inline-flex w-fit items-center gap-2 px-3 py-1.5 text-[13px] font-medium border border-[#333] rounded-full bg-[#111] mb-8">
                <span className="text-gray-300">Available on GitHub</span>
              </div>

              <h2 className="text-4xl md:text-5xl font-bold tracking-tight mb-6 leading-tight text-balance">
                Built for engineers who want to understand the stack, not hide from it.
              </h2>

              <p className="text-lg text-gray-400 mb-10 leading-relaxed text-balance">
                The framework avoids reflection and hidden dependency injection containers. Authored by <strong>Jhanvi Patel</strong>, NioFlow ensures all dependencies and middleware flows are explicit and predictable.
              </p>

              <BenchmarkStats />

              <div className="flex flex-col sm:flex-row gap-4 mt-12">

                <Link href="/docs" className="inline-flex justify-center items-center h-12 px-8 text-[15px] font-medium bg-white text-black hover:bg-gray-200 transition-colors rounded-lg shadow-md">
                  Read Technical Summary
                </Link>
                <Link href="https://github.com/jhanvi857/coreHTTP" target="_blank" className="inline-flex justify-center items-center h-12 px-8 text-[15px] font-medium bg-transparent text-white border border-[#333] hover:bg-[#111] transition-colors rounded-lg">
                  Star repository
                </Link>
              </div>
            </div>

            {/* Right Visual / Meta Column */}
            <div className="relative w-full h-full min-h-[400px] flex flex-col items-center justify-center">
              <DependencyTabs />
            </div>

          </div>
        </section>

      </main>

      <Footer />
    </div>
  );
}
