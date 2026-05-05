import { SectionCard, Pagination } from "./_components";
import Link from "next/link";


export default function DocsHomePage() {
  return (
    <>
      <div className="mb-12">
        <div className="inline-flex items-center rounded-full border border-muted bg-muted/50 px-3 py-1 text-xs font-medium text-gray-600 dark:text-gray-300 mb-5">
          NioFlow Documentation
        </div>
        <h1 className="text-4xl md:text-5xl font-bold tracking-tight mb-4 text-gray-900 dark:text-white text-center md:text-left">
          Professional Framework Guide
        </h1>
        <p className="text-lg text-gray-600 dark:text-gray-400 leading-relaxed text-justify">
          Start from installation and move step-by-step to routing, authentication, frontend integration,
          deployment, and production hardening.
        </p>
      </div>

      <section className="mb-10 rounded-2xl border border-muted bg-surface p-6 md:p-8">
        <h2 className="text-2xl font-bold tracking-tight text-gray-900 dark:text-white mb-4">What You Will Build</h2>
        <p className="text-gray-600 dark:text-gray-400 leading-relaxed mb-4 text-justify">
          This guide is focused on practical delivery. By the end, you will have a real Java backend using
          NioFlow routes, structured middleware, JWT-protected APIs, health and readiness probes, and a cloud-ready
          runtime setup that can be deployed to Docker, Render, or Railway.
        </p>
        <p className="text-gray-600 dark:text-gray-400 leading-relaxed text-justify">
          Every section includes copy-paste examples and production defaults so you can move from local proof-of-concept
          to release candidate without rewriting the architecture.
        </p>
      </section>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-8">
        <SectionCard
          href="/docs/getting-started"
          title="1. Getting Started"
          description="Download options, Maven vs manual setup, project bootstrap, and port registration strategy."
        />
        <SectionCard
          href="/docs/routing-frontend"
          title="2. Routing + Frontend"
          description="Build custom routes, parse request body/path params, and integrate with frontend fetch clients."
        />
        <SectionCard
          href="/docs/auth-security"
          title="3. Auth + Security"
          description="Implement signup/login, generate JWT tokens, protect route groups, and set security baselines."
        />
        <SectionCard
          href="/docs/database-env"
          title="4. Database + Env"
          description="Orchestrate secrets with .env files, connect to Supabase/Postgres, and manage connection pools."
        />
        <SectionCard
          href="/docs/deployment"
          title="5. Operations + Deployment"
          description="Operational readiness, deploy strategy, probes, incident basics, and release delivery workflow."
        />
        <SectionCard
          href="/docs/reference"
          title="6. Reference"
          description="Configuration matrix, endpoint reference, starter zip endpoint design, and troubleshooting."
        />
        <SectionCard
          href="/docs/performance"
          title="7. Performance"
          description="Load test results, throughput analysis, latency matrix, and hardware validation reports."
        />
      </div>


      <section className="grid grid-cols-1 lg:grid-cols-3 gap-4 mb-8">
        <article className="rounded-xl border border-muted bg-surface p-5">
          <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-2">Guided Learning Path</h3>
          <p className="text-sm text-gray-600 dark:text-gray-400 leading-relaxed text-justify">
            Follow sections in order. Each page prepares the next, ensuring no environment or deployment prerequisites are missed.
          </p>
        </article>
        <article className="rounded-xl border border-muted bg-surface p-5">
          <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-2">Production-Readiness</h3>
          <p className="text-sm text-gray-600 dark:text-gray-400 leading-relaxed text-justify">
            Examples are complete, not pseudo-code.Copy snippets directly into your project and adapt structure as needed.
          </p>
        </article>
        <article className="rounded-xl border border-muted bg-surface p-5">
          <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-2">Security-First Focus</h3>
          <p className="text-sm text-gray-600 dark:text-gray-400 leading-relaxed text-justify">
            Docs prioritize safe defaults: explicit CORS, strong JWT secrets, and health probes by default.
          </p>
        </article>
      </section>

      <section className="rounded-2xl border border-muted bg-surface p-6 md:p-8 mb-4">
        <h2 className="text-2xl font-bold tracking-tight text-gray-900 dark:text-white mb-4">Architecture At A Glance</h2>
        <div className="mb-5 rounded-xl border border-muted bg-[#0e0e11] p-4 md:p-5 overflow-x-auto">
          <pre className="text-xs md:text-sm text-gray-200 leading-relaxed">
            {`Client
  -> Selector accept loop
  -> Accepted SocketChannel (blocking mode for parser)
  -> Bounded worker pool
  -> HttpParser
  -> Router
  -> Middleware chain
  -> Handler / Plugin
  -> HttpResponse
  -> Client`}
          </pre>
        </div>
        <ol className="list-decimal pl-6 space-y-3 text-gray-600 dark:text-gray-400 leading-relaxed">
          <li>
            Requests are accepted by the NIO selector loop and then processed on bounded workers.
          </li>
          <li>
            Middleware executes in registration order for logging, CORS, metrics, rate limiting, and auth.
          </li>
          <li>
            Handlers operate on a typed HTTP context and return JSON/text with explicit status control.
          </li>
          <li>
            Optional persistence and asynchronous work offload keep IO handling responsive.
          </li>
          <li>
            Ops endpoints (health, readiness, metrics) integrate with monitors and rollout checks.
          </li>
        </ol>
      </section>

      <section className="rounded-2xl border border-muted bg-surface p-6 md:p-8">
        <div className="flex items-center justify-between mb-6">
          <h2 className="text-2xl font-bold tracking-tight text-gray-900 dark:text-white">Performance Baselines</h2>
          <Link href="/docs/performance" className="text-sm font-medium text-blue-600 hover:underline">View full report →</Link>
        </div>
        <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
          <div className="p-4 rounded-xl border border-muted bg-muted/20">
            <div className="text-xs text-gray-500 uppercase tracking-wider mb-1">Throughput</div>
            <div className="text-xl font-bold text-gray-900 dark:text-white">501 req/s</div>
          </div>
          <div className="p-4 rounded-xl border border-muted bg-muted/20">
            <div className="text-xs text-gray-500 uppercase tracking-wider mb-1">p50 Latency</div>
            <div className="text-xl font-bold text-gray-900 dark:text-white">1.52 ms</div>
          </div>
          <div className="p-4 rounded-xl border border-muted bg-muted/20">
            <div className="text-xs text-gray-500 uppercase tracking-wider mb-1">p99 Latency</div>
            <div className="text-xl font-bold text-gray-900 dark:text-white">74.6 ms</div>
          </div>
          <div className="p-4 rounded-xl border border-muted bg-muted/20">
            <div className="text-xs text-gray-500 uppercase tracking-wider mb-1">Success Rate</div>
            <div className="text-xl font-bold text-gray-900 dark:text-white">99.84%</div>
          </div>
        </div>
      </section>

      <Pagination
        next={{ href: "/docs/getting-started", label: "Getting Started" }}
      />
    </>
  );
}
