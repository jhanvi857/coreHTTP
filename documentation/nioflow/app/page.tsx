import Link from "next/link";
import CodeDemo from "./components/CodeDemo";
import Footer from "./components/Footer";
import Navbar from "./components/Navbar";

export default function Home() {
  return (
    <div className="app-shell bg-[#050505] text-white selection:bg-blue-500/30">
      <Navbar />

      <main className="flex-1 w-full flex flex-col items-center">
        {/* HERO SECTION */}
        <section className="relative w-full pt-32 pb-24 flex flex-col items-center justify-center border-b border-[#1a1a1a] overflow-hidden bg-black/80">
          {/* Subtle Grid Background */}
          {/* <div className="absolute inset-0 bg-[linear-gradient(rgba(255,255,255,0.03)_1px,transparent_1px),linear-gradient(90deg,rgba(255,255,255,0.03)_1px,transparent_1px)] bg-[size:60px_60px] pointer-events-none" /> */}

          <div className="relative z-10 max-w-6xl mx-auto px-6 w-full flex flex-col lg:flex-row items-center gap-16">

            <div className="flex-1 flex flex-col items-start text-left">
              <div className="inline-flex items-center gap-2 px-3 py-1.5 text-[13px] font-medium border border-[#333] rounded-full bg-[#111] mb-8">
                <span className="flex h-2 w-2 rounded-full bg-blue-500 shadow-[0_0_8px_rgba(59,130,246,0.8)]" />
                <span className="text-gray-300">Java NIO Systems Engineering</span>
              </div>

              <h1 className="text-5xl lg:text-6xl font-bold tracking-tight text-white leading-tight mb-6">
                NioFlow: A Configurable Java <span className="text-transparent bg-clip-text bg-gradient-to-r from-gray-200 to-gray-600">Micro-Framework.</span>
              </h1>

              <p className="text-lg text-gray-400 mb-10 leading-relaxed max-w-xl">
                NioFlow is a minimalistic Java HTTP framework focusing on explicit programmatic configuration. It utilizes a hybrid architecture: NIO Selector for connection acceptance and a bounded thread pool for blocking request processing.
              </p>

              <div className="flex flex-col sm:flex-row gap-4 w-full sm:w-auto">
                <Link href="/docs" className="inline-flex justify-center items-center h-12 px-8 text-[15px] font-medium bg-white text-black hover:bg-gray-200 transition-colors rounded-lg shadow-md">
                  Read Architecture Docs
                </Link>
                <Link href="https://github.com/jhanvi857/nioflow" target="_blank" className="inline-flex justify-center items-center h-12 px-8 text-[15px] font-medium bg-transparent text-white border border-[#333] hover:bg-[#111] transition-colors rounded-lg">
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
                  <p><span className="text-green-400">$</span> ./scripts/run.ps1</p>
                  <p className="text-gray-500 mt-2">[INFO] Bootstrap: Initializing Server Components...</p>
                  <p className="text-gray-500">[INFO] Database: HikariCP pool establishing 10 local connections to PostgreSQL</p>
                  <p className="text-gray-500">[INFO] NIO: Registering ServerSocketChannel on 0.0.0.0:8080</p>
                  <p className="text-blue-400 mb-4">[READY] Selector Event Loop started successfully.</p>
                  <p className="mt-4"><span className="text-green-400">$</span> curl -X GET http://localhost:8080/api/health</p>
                  <p className="text-yellow-400 mt-2">HTTP/1.1 200 OK</p>
                  <p className="text-gray-400">Content-Type: application/json</p>
                  <p className="text-gray-300 mt-2">{`{ "status": "UP", "uptime": "0ms" }`}</p>
                </div>
              </div>
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

            <div className="grid md:grid-cols-2 gap-8">
              {/* Feature 1 */}
              <div className="p-10 rounded-2xl border border-[#222] bg-[#111] hover:bg-[#151515] transition-colors group">
                <div className="w-12 h-12 rounded-lg bg-[#1a1a1a] border border-[#333] flex items-center justify-center font-mono text-xs text-gray-400 mb-8 transition-transform group-hover:scale-110">
                  NIO
                </div>
                <h3 className="text-2xl font-bold mb-4 text-white">Hybrid NIO/Blocking Architecture</h3>
                <p className="text-gray-400 leading-relaxed text-[16px]">
                  Built on Java's <code className="text-gray-300 bg-[#222] px-1.5 py-0.5 rounded text-sm">java.nio</code> module for the <code>ServerSocketChannel</code>. A single event loop accepts connections using a Selector, then hands off the blocking <code>SocketChannel</code> read/write operations to a <code>ThreadPoolExecutor</code>. Under high load, the bounded queue safely rejects extra connections.
                </p>
              </div>

              {/* Feature 2 */}
              <div className="p-10 rounded-2xl border border-[#222] bg-[#111] hover:bg-[#151515] transition-colors group">
                <div className="w-12 h-12 rounded-lg bg-[#1a1a1a] border border-[#333] flex items-center justify-center font-mono text-xs text-gray-400 mb-8 transition-transform group-hover:scale-110">
                  DMA
                </div>
                <h3 className="text-2xl font-bold mb-4 text-white">Zero-Copy Memory Transfers</h3>
                <p className="text-gray-400 leading-relaxed text-[16px]">
                  Implements hardware-accelerated Direct Memory Access (DMA). File payloads are transferred directly from disk cache to the network socket layer via <code className="text-gray-300 bg-[#222] px-1.5 py-0.5 rounded text-sm">FileChannel.transferTo()</code> without ever crossing into user-space memory.
                </p>
              </div>

              {/* Feature 3 */}
              <div className="p-10 rounded-2xl border border-[#222] bg-[#111] hover:bg-[#151515] transition-colors group">
                <div className="w-12 h-12 rounded-lg bg-[#1a1a1a] border border-[#333] flex items-center justify-center font-mono text-xs text-gray-400 mb-8 transition-transform group-hover:scale-110">
                  MID
                </div>
                <h3 className="text-2xl font-bold mb-4 text-white">Chain of Responsibility</h3>
                <p className="text-gray-400 leading-relaxed text-[16px]">
                  Requests traverse a strict middleware pipeline before reaching controllers. Features structured logging pipelines, token-bucket rate limiters, and JWT-based authentication blocks injected dynamically on boot.
                </p>
              </div>

              {/* Feature 4 */}
              <div className="p-10 rounded-2xl border border-[#222] bg-[#111] hover:bg-[#151515] transition-colors group">
                <div className="w-12 h-12 rounded-lg bg-[#1a1a1a] border border-[#333] flex items-center justify-center font-mono text-xs text-gray-400 mb-8 transition-transform group-hover:scale-110">
                  JBDC
                </div>
                <h3 className="text-2xl font-bold mb-4 text-white">Robust Persistence</h3>
                <p className="text-gray-400 leading-relaxed text-[16px]">
                  A hardened data access layer utilizing HikariCP to aggressively pool database connections. Paired with strict prepared statements and PostgreSQL to handle tens of thousands of secure transactions safely.
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
                The Final Piece of the <span className="text-transparent bg-clip-text bg-gradient-to-r from-gray-200 to-gray-600">Architecture Puzzle.</span>
              </h2>

              <p className="text-lg text-gray-400 mb-10 leading-relaxed text-balance">
                The framework avoids reflection and hidden dependency injection containers. All route logic, exception handling, and middleware flows are wired manually, ensuring completely predictable code execution.
              </p>

              <div className="flex flex-col sm:flex-row gap-4">
                <Link href="/docs" className="inline-flex justify-center items-center h-12 px-8 text-[15px] font-medium bg-white text-black hover:bg-gray-200 transition-colors rounded-lg shadow-md">
                  Read Technical Summary
                </Link>
                <Link href="https://github.com/jhanvi857/nioflow" target="_blank" className="inline-flex justify-center items-center h-12 px-8 text-[15px] font-medium bg-transparent text-white border border-[#333] hover:bg-[#111] transition-colors rounded-lg">
                  Star repository
                </Link>
              </div>
            </div>

            {/* Right Visual / Meta Column */}
            <div className="relative w-full h-full min-h-[400px] rounded-2xl border border-[#222] bg-[#111] overflow-hidden flex flex-col items-center justify-center p-8 group">
              <div className="absolute inset-0 bg-[linear-gradient(rgba(255,255,255,0.03)_1px,transparent_1px),linear-gradient(90deg,rgba(255,255,255,0.03)_1px,transparent_1px)] bg-[size:40px_40px] pointer-events-none" />

              <div className="relative z-10 w-full max-w-sm rounded-xl border border-[#333] bg-black/80 backdrop-blur-md shadow-2xl p-6">
                <div className="flex items-center justify-between border-b border-[#333] pb-4 mb-4">
                  <div className="text-xs font-mono text-gray-500 uppercase tracking-widest">Stack Manifest</div>
                </div>
                <div className="space-y-4 font-mono text-[13px]">
                  <div className="flex justify-between items-center">
                    <span className="text-gray-500">Core Engine</span>
                    <span className="text-gray-300">Native java.nio</span>
                  </div>
                  <div className="flex justify-between items-center">
                    <span className="text-gray-500">Data Pipeline</span>
                    <span className="text-gray-300">FileChannel DMA</span>
                  </div>
                  <div className="flex justify-between items-center">
                    <span className="text-gray-500">Persistence</span>
                    <span className="text-gray-300">PostgreSQL 15+</span>
                  </div>
                  <div className="flex justify-between items-center">
                    <span className="text-gray-500">Cryptography</span>
                    <span className="text-gray-300">BCrypt / JWT</span>
                  </div>
                  <div className="pt-4 mt-2 border-t border-[#333] flex justify-between items-center text-blue-400 font-semibold">
                    <span>License</span>
                    <span>MIT</span>
                  </div>
                </div>
              </div>

            </div>

          </div>
        </section>

      </main>

      <Footer />
    </div>
  );
}
