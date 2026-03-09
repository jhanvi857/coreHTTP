import Footer from "../components/Footer";
import Navbar from "../components/Navbar";
import { roadmapMilestones } from "../lib/docsContent";

export default function RoadmapPage() {
  return (
    <div className="app-shell bg-[#050505] text-white selection:bg-blue-500/30 min-h-screen">
      <Navbar />

      {/* Background Gradients */}
      <div className="absolute inset-0 bg-[linear-gradient(rgba(255,255,255,0.03)_1px,transparent_1px),linear-gradient(90deg,rgba(255,255,255,0.03)_1px,transparent_1px)] bg-[size:60px_60px] pointer-events-none" />

      <main className="w-full max-w-[1000px] mx-auto px-6 py-16 md:py-24 relative z-10 flex-1">
        <header className="mb-16 border-b border-[#1a1a1a] pb-12">
          <div className="inline-flex items-center gap-2 px-3 py-1.5 text-[13px] font-medium border border-[#333] rounded-full bg-[#111] mb-8 shadow-sm">
            <span className="text-gray-400 font-mono text-[11px] tracking-widest uppercase">Roadmap Projection</span>
          </div>
          <h1 className="text-4xl md:text-5xl font-bold tracking-tight mb-6">Delivery Milestones</h1>
          <p className="max-w-3xl text-lg text-gray-400 leading-relaxed">
            coreHTTP milestones reflect deployment readiness, operational stability, and sustained
            developer velocity across the service lifecycle.
          </p>
        </header>

        <section className="relative ml-4 md:ml-6 before:absolute before:-left-4 md:before:-left-6 before:top-8 before:bottom-0 before:w-px before:bg-[#222]">
          {roadmapMilestones.map((milestone, index) => {
            const isCompleted = milestone.phase.includes("COMPLETED");
            return (
              <article key={milestone.phase} className="relative mb-12">
                {/* Timeline Node */}
                <div className={`absolute -left-[20px] md:-left-[28px] top-8 h-2.5 w-2.5 rounded-none rotate-45 border border-[#333] ${isCompleted ? "bg-white shadow-[0_0_10px_rgba(255,255,255,0.5)]" : "bg-[#111]"}`} />

                <div className={`rounded-2xl border bg-[#111] p-6 md:p-8 ml-6 transition-all shadow-xl ${isCompleted ? "border-[#444]" : "border-[#222] opacity-80"}`}>
                  <div className="flex flex-col sm:flex-row sm:items-start justify-between mb-6 gap-4 border-b border-[#222] pb-6">
                    <div>
                      <p className="text-[11px] font-bold font-mono tracking-widest text-gray-500 mb-3 uppercase">{milestone.phase}</p>
                      <h2 className="text-2xl font-bold text-white tracking-tight">{milestone.goal}</h2>
                    </div>
                    {isCompleted ? (
                      <span className="inline-flex items-center px-3 py-1 rounded bg-[#0a0a0a] border border-[#333] text-[11px] font-mono text-gray-300 tracking-widest">
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
                </div>
              </article>
            );
          })}
        </section>
      </main>

      <Footer />
    </div>
  );
}
