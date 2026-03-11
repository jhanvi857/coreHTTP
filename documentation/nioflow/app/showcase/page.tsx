import Footer from "../components/Footer";
import Navbar from "../components/Navbar";

const trafficBars = [42, 68, 51, 74, 62, 87, 55, 79, 92, 83, 58, 66, 72, 89, 77, 94, 86, 73, 91, 96];

export default function ShowcasePage() {
  return (
    <div className="app-shell bg-[#050505] text-white selection:bg-blue-500/30 min-h-screen">
      <Navbar />

      {/* Background Gradients */}
      <div className="absolute inset-0 bg-[linear-gradient(rgba(255,255,255,0.03)_1px,transparent_1px),linear-gradient(90deg,rgba(255,255,255,0.03)_1px,transparent_1px)] bg-[size:60px_60px] pointer-events-none" />

      <main className="w-full max-w-[1200px] mx-auto px-6 py-16 md:py-24 relative z-10">
        <header className="mb-16 border-b border-[#1a1a1a] pb-12">
          <div className="inline-flex items-center gap-2 px-3 py-1.5 text-[13px] font-medium border border-[#333] rounded-full bg-[#111] mb-8 shadow-sm">
            <span className="flex h-2 w-2 rounded-full bg-red-500 shadow-[0_0_8px_rgba(239,68,68,0.8)] animate-pulse" />
            <span className="text-gray-300">Operations Console</span>
          </div>
          <h1 className="text-4xl md:text-5xl font-bold tracking-tight mb-6">Live Runtime Snapshot</h1>
          <p className="max-w-3xl text-lg text-gray-400 leading-relaxed">
            Operational visibility for traffic, infrastructure health, and middleware execution path.
            This view mirrors what production teams monitor during rollout and incident response.
          </p>
        </header>

        <section className="mb-12 grid grid-cols-1 gap-6 xl:grid-cols-3">
          <article className="rounded-2xl border border-[#222] bg-[#111] p-8 xl:col-span-2 flex flex-col shadow-xl">
            <div className="mb-8 flex items-center justify-between border-b border-[#222] pb-6">
              <h2 className="text-xl font-bold tracking-tight text-white">Traffic Throughput</h2>
              <span className="rounded bg-[#0a0a0a] border border-[#333] px-3 py-1 text-xs font-mono text-gray-500">Updated: 5s</span>
            </div>

            <div className="mb-8 flex h-56 items-end gap-[4px] rounded-xl bg-[#0a0a0a] p-6 border border-[#222] overflow-hidden">
              {trafficBars.map((height, index) => (
                <div
                  key={`${height}-${index}`}
                  className="flex-1 bg-gradient-to-t from-blue-900/50 to-blue-500 hover:from-blue-600 hover:to-blue-400 transition-colors opacity-80"
                  style={{ height: `${height}%` }}
                />
              ))}
            </div>

            <div className="grid grid-cols-3 gap-6 pt-6 border-t border-[#222] mt-auto">
              <div>
                <p className="text-[11px] font-bold text-gray-500 font-mono tracking-widest mb-2 uppercase">Requests</p>
                <p className="text-3xl font-bold text-gray-200 tracking-tight">1.2M</p>
              </div>
              <div>
                <p className="text-[11px] font-bold text-gray-500 font-mono tracking-widest mb-2 uppercase">P95 Latency</p>
                <p className="text-3xl font-bold text-gray-200 tracking-tight">6.1ms</p>
              </div>
              <div>
                <p className="text-[11px] font-bold text-gray-500 font-mono tracking-widest mb-2 uppercase">Error Rate</p>
                <p className="text-3xl font-bold text-green-500 tracking-tight drop-shadow-[0_0_8px_rgba(34,197,94,0.4)]">0.01%</p>
              </div>
            </div>
          </article>

          <div className="space-y-6 flex flex-col">
            <article className="rounded-2xl border border-[#222] bg-[#111] p-8 flex-1 shadow-xl">
              <h3 className="mb-6 text-lg font-bold tracking-tight text-white border-b border-[#222] pb-4">Infrastructure Health</h3>
              <div className="space-y-5 text-sm font-medium">
                <div className="flex items-center justify-between">
                  <span className="text-gray-400 font-mono text-[13px]">NIO Selector</span>
                  <span className="text-green-500 font-bold flex items-center gap-2 text-[11px] tracking-widest">
                    <span className="w-2 h-2 rounded-full bg-green-500 drop-shadow-[0_0_4px_rgba(34,197,94,0.8)]" />
                    ACTIVE
                  </span>
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-gray-400 font-mono text-[13px]">PostgreSQL Pool</span>
                  <span className="text-green-500 font-bold flex items-center gap-2 text-[11px] tracking-widest">
                    <span className="w-2 h-2 rounded-full bg-green-500 drop-shadow-[0_0_4px_rgba(34,197,94,0.8)]" />
                    HEALTHY
                  </span>
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-gray-400 font-mono text-[13px]">Runtime Memory</span>
                  <span className="font-mono text-xs bg-[#0a0a0a] border border-[#333] px-2 py-1 rounded text-gray-300">124MB / 512MB</span>
                </div>
              </div>
            </article>

            <article className="rounded-2xl border border-[#222] bg-[#111] p-8 shadow-xl">
              <h3 className="mb-6 text-lg font-bold tracking-tight text-white border-b border-[#222] pb-4">Security Layer</h3>
              <div className="space-y-4 text-sm font-mono">
                <div className="flex items-center justify-between bg-[#0a0a0a] border border-[#222] rounded-lg px-4 py-3">
                  <span className="text-gray-300 text-[13px]">JWT Provider</span>
                  <span className="text-blue-400 text-[11px] tracking-widest">v0.12.5</span>
                </div>
                <div className="flex items-center justify-between bg-[#0a0a0a] border border-[#222] rounded-lg px-4 py-3">
                  <span className="text-gray-300 text-[13px]">Rate Limiter</span>
                  <span className="text-purple-400 text-[11px] tracking-widest drop-shadow-[0_0_4px_rgba(168,85,247,0.4)]">ENABLED</span>
                </div>
                <div className="flex items-center justify-between bg-[#0a0a0a] border border-[#222] rounded-lg px-4 py-3">
                  <span className="text-gray-300 text-[13px]">BCrypt Hashing</span>
                  <span className="text-green-400 text-[11px] tracking-widest">ACTIVE</span>
                </div>
              </div>
            </article>
          </div>
        </section>

        <section className="grid gap-6 lg:grid-cols-2 mt-8">
          <article className="rounded-2xl border border-[#222] bg-[#111] p-8 shadow-xl">
            <h3 className="mb-8 text-xl font-bold tracking-tight text-white border-b border-[#222] pb-4">Middleware Lifecycle</h3>
            <div className="grid gap-4 text-center sm:grid-cols-4">
              <div className="border border-[#333] bg-[#0a0a0a] rounded-xl px-3 py-6 flex flex-col items-center justify-center relative hover:bg-[#151515] transition-colors">
                <div className="absolute right-0 top-1/2 -mt-px w-4 h-px bg-[#333] hidden sm:block translate-x-full"></div>
                <p className="text-[10px] font-bold font-mono tracking-widest text-gray-500">STAGE_1</p>
                <p className="mt-3 text-[14px] text-gray-200">Logger</p>
              </div>
              <div className="border border-[#333] bg-[#0a0a0a] rounded-xl px-3 py-6 flex flex-col items-center justify-center relative hover:bg-[#151515] transition-colors">
                <div className="absolute right-0 top-1/2 -mt-px w-4 h-px bg-[#333] hidden sm:block translate-x-full"></div>
                <p className="text-[10px] font-bold font-mono tracking-widest text-gray-500">STAGE_2</p>
                <p className="mt-3 text-[14px] text-gray-200">Rate Limit</p>
              </div>
              <div className="border border-[#333] bg-[#0a0a0a] rounded-xl px-3 py-6 flex flex-col items-center justify-center relative hover:bg-[#151515] transition-colors">
                <div className="absolute right-0 top-1/2 -mt-px w-4 h-px bg-[#333] hidden sm:block translate-x-full"></div>
                <p className="text-[10px] font-bold font-mono tracking-widest text-gray-500">STAGE_3</p>
                <p className="mt-3 text-[14px] text-gray-200">Auth Guard</p>
              </div>
              <div className="rounded-xl border border-blue-500/50 bg-blue-900/20 px-3 py-6 flex flex-col items-center justify-center relative overflow-hidden shadow-[0_0_15px_rgba(59,130,246,0.1)]">
                <div className="absolute inset-x-0 bottom-0 h-1 bg-blue-500"></div>
                <p className="text-[10px] font-bold font-mono tracking-widest text-blue-400">FINAL</p>
                <p className="mt-3 text-[14px] text-white">Handler</p>
              </div>
            </div>
          </article>

          <article className="rounded-2xl border border-[#222] bg-[#111] p-8 shadow-xl flex flex-col">
            <h3 className="mb-4 text-xl font-bold tracking-tight text-white border-b border-[#222] pb-4">SQL Pool Profile</h3>
            <p className="mb-8 text-[15px] text-gray-400 leading-relaxed font-mono">
              HikariCP-backed pooling keeps query latency stable while preserving transactional integrity under load.
            </p>
            <div className="grid grid-cols-3 gap-6 text-center font-mono text-sm mt-auto">
              <div className="border border-[#333] bg-[#0a0a0a] py-6 rounded-xl flex flex-col gap-2 items-center justify-center">
                <span className="text-gray-500 text-[11px] tracking-widest">IDLE_CONN</span>
                <span className="font-bold text-2xl text-gray-300">10</span>
              </div>
              <div className="border border-blue-500/30 bg-blue-500/10 py-6 rounded-xl flex flex-col gap-2 items-center justify-center relative shadow-[inset_0_0_20px_rgba(59,130,246,0.05)]">
                <span className="absolute top-2 right-2 w-1.5 h-1.5 rounded-full bg-blue-500 animate-pulse"></span>
                <span className="text-blue-400 text-[11px] tracking-widest">ACTIVE_CONN</span>
                <span className="font-bold text-2xl text-white">2</span>
              </div>
              <div className="border border-[#333] bg-[#0a0a0a] py-6 rounded-xl flex flex-col gap-2 items-center justify-center">
                <span className="text-gray-500 text-[11px] tracking-widest">MAX_CONN</span>
                <span className="font-bold text-2xl text-gray-300">20</span>
              </div>
            </div>
          </article>
        </section>
      </main>

      <Footer />
    </div>
  );
}
