import Footer from "../components/Footer";
import Navbar from "../components/Navbar";
import { roadmapMilestones } from "../lib/docsContent";

export default function RoadmapPage() {
  return (
    <div className="app-shell bg-[#050505] text-white selection:bg-blue-500/30 min-h-screen">
      <Navbar />

      {/* Background Gradients */}
      <div className="absolute inset-0 bg-[linear-gradient(rgba(255,255,255,0.03)_1px,transparent_1px),linear-gradient(90deg,rgba(255,255,255,0.03)_1px,transparent_1px)] bg-size-[60px_60px] pointer-events-none" />

      <main className="w-full max-w-250 mx-auto px-6 py-16 md:py-24 relative z-10 flex-1">
        <header className="mb-16 border-b border-[#1a1a1a] pb-12">
          <div className="inline-flex items-center gap-2 px-3 py-1.5 text-[13px] font-medium border border-[#333] rounded-full bg-[#111] mb-8 shadow-sm">
            <span className="text-gray-400 font-mono text-[11px] tracking-widest uppercase">Project Roadmap</span>
          </div>
          <h1 className="text-4xl md:text-5xl font-bold tracking-tight mb-6">Development Milestones</h1>
          <p className="max-w-3xl text-lg text-gray-400 leading-relaxed">
            NioFlow evolved in three deliberate phases, each focused on a specific engineering concern: core execution,
            framework ergonomics, and production safety. This roadmap explains not only what was built, but why each
            phase matters for real-world teams.
          </p>
        </header>

        <section className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-14">
          <article className="rounded-xl border border-[#202020] bg-[#101010] p-5">
            <h2 className="text-base font-semibold tracking-tight mb-2">Execution Foundation</h2>
            <p className="text-sm text-gray-400 leading-relaxed">
              Built a non-blocking, selector-driven runtime to ensure concurrency and responsiveness under load.
            </p>
          </article>
          <article className="rounded-xl border border-[#202020] bg-[#101010] p-5">
            <h2 className="text-base font-semibold tracking-tight mb-2">Developer Experience</h2>
            <p className="text-sm text-gray-400 leading-relaxed">
              Added routing, middleware composition, and payload handling so product teams can ship APIs quickly.
            </p>
          </article>
          <article className="rounded-xl border border-[#202020] bg-[#101010] p-5">
            <h2 className="text-base font-semibold tracking-tight mb-2">Operational Confidence</h2>
            <p className="text-sm text-gray-400 leading-relaxed">
              Hardened error behavior, TLS support, graceful shutdown, and observability for production rollouts.
            </p>
          </article>
        </section>

        <section className="rounded-2xl border border-[#202020] bg-[#101010] p-6 md:p-8 mb-14">
          <h2 className="text-2xl font-bold tracking-tight mb-4">How To Read This Roadmap</h2>
          <p className="text-gray-400 leading-relaxed mb-4">
            Each phase contains capabilities that were delivered together for architectural coherence. The sequence is
            intentional: we first stabilized concurrency primitives, then built higher-level framework APIs, and then
            finalized deployment and reliability behavior.
          </p>
          <p className="text-gray-400 leading-relaxed">
            For teams adopting NioFlow, this means you can trust both sides of the stack: fast request handling and
            operational safety characteristics expected in production environments.
          </p>
        </section>

        <section className="relative ml-4 md:ml-6 before:absolute before:-left-4 md:before:-left-6 before:top-8 before:bottom-0 before:w-px before:bg-[#222]">
          {roadmapMilestones.map((milestone, index) => {
            const isCompleted = milestone.phase.includes("COMPLETED");
            return (
              <article key={milestone.phase} className="relative mb-12">
                {/* Timeline Node */}
                <div className={`absolute -left-5 md:-left-7 top-8 h-2.5 w-2.5 rounded-none rotate-45 border border-[#333] ${isCompleted ? "bg-white shadow-[0_0_10px_rgba(255,255,255,0.5)]" : "bg-[#111]"}`} />

                <div className={`rounded-2xl border bg-[#111] p-6 md:p-8 ml-6 transition-all shadow-xl ${isCompleted ? "border-[#444]" : "border-[#222] opacity-80"}`}>
                  <div className="flex flex-col sm:flex-row sm:items-start justify-between mb-6 gap-4 border-b border-[#222] pb-6">
                    <div>
                      <p className="text-[11px] font-bold font-mono tracking-widest text-gray-500 mb-3 uppercase">{milestone.phase}</p>
                      <h2 className="text-2xl font-bold text-white tracking-tight">{milestone.goal}</h2>
                    </div>
                    {isCompleted ? (
                      <span className="inline-flex items-center px-3 py-1 rounded bg-card-dark border border-[#333] text-[11px] font-mono text-gray-300 tracking-widest">
                        [ COMPLETED ]
                      </span>
                    ) : (
                      <span className="inline-flex items-center px-3 py-1 rounded bg-blue-900/20 border border-blue-500/30 text-[11px] font-mono text-blue-400 tracking-widest">
                        [ PLANNED ]
                      </span>
                    )}
                  </div>

                  <ul className="space-y-4">
                    {milestone.deliverables.map((item) => (
                      <li key={item} className="flex items-start gap-4 text-gray-400 text-[15px] leading-relaxed">
                        <span className="text-[#444] font-mono mt-0.5">{`>`}</span>
                        <span>{item}</span>
                      </li>
                    ))}
                  </ul>

                  <div className="mt-6 border-t border-[#222] pt-5">
                    <p className="text-sm text-gray-500 leading-relaxed">
                      {index === 0 &&
                        "Why this phase matters: The engine layer determines baseline scalability. By establishing a clear request model and controlled worker handoff early, NioFlow avoids hidden concurrency behavior later in application code."}
                      {index === 1 &&
                        "Why this phase matters: Framework-level abstractions reduce boilerplate and keep business logic focused. Middleware and routing guarantees make services easier to maintain as endpoint count grows."}
                      {index === 2 &&
                        "Why this phase matters: Production incidents are often operational, not functional. TLS, graceful shutdown, and centralized error handling dramatically improve deploy safety and recovery speed."}
                    </p>
                  </div>
                </div>
              </article>
            );
          })}
        </section>

        <section className="rounded-2xl border border-[#202020] bg-[#101010] p-6 md:p-8 mt-8">
          <h2 className="text-2xl font-bold tracking-tight mb-4">Production Hardening</h2>
          <ul className="space-y-4">
            <li className="flex items-start gap-4 text-gray-400 text-[15px] leading-relaxed">
              <span className="text-[#444] font-mono mt-0.5">{`>`}</span>
              <span>Add end-to-end tests with real PostgreSQL in CI service containers.</span>
            </li>
            <li className="flex items-start gap-4 text-gray-400 text-[15px] leading-relaxed">
              <span className="text-[#444] font-mono mt-0.5">{`>`}</span>
              <span>Add configurable auth claim mapping for role-based authorization.</span>
            </li>
            <li className="flex items-start gap-4 text-gray-400 text-[15px] leading-relaxed">
              <span className="text-[#444] font-mono mt-0.5">{`>`}</span>
              <span>Implement automated OpenAPI/Swagger generation from explicit routes.</span>
            </li>
          </ul>
        </section>
      </main>

      <Footer />
    </div>
  );
}
