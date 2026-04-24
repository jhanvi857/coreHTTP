import { SectionCard } from "./_components";

export default function DocsHomePage() {
  return (
    <>
      <div className="mb-12">
        <div className="inline-flex items-center rounded-full border border-muted bg-muted/50 px-3 py-1 text-xs font-medium text-gray-600 dark:text-gray-300 mb-5">
          NioFlow Documentation
        </div>
        <h1 className="text-4xl md:text-5xl font-bold tracking-tight mb-4 text-gray-900 dark:text-white">
          Professional Framework Guide
        </h1>
        <p className="text-lg text-gray-600 dark:text-gray-400 leading-relaxed">
          Start from installation and move step-by-step to routing, authentication, frontend integration,
          deployment, and production hardening.
        </p>
      </div>

      <section className="mb-10 rounded-2xl border border-muted bg-surface p-6 md:p-8">
        <h2 className="text-2xl font-bold tracking-tight text-gray-900 dark:text-white mb-4">What You Will Build</h2>
        <p className="text-gray-600 dark:text-gray-400 leading-relaxed mb-4">
          This guide is focused on practical delivery. By the end, you will have a real Java backend using
          NioFlow routes, structured middleware, JWT-protected APIs, health and readiness probes, and a cloud-ready
          runtime setup that can be deployed to Docker, Render, or Railway.
        </p>
        <p className="text-gray-600 dark:text-gray-400 leading-relaxed">
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
      </div>

      <section className="grid grid-cols-1 lg:grid-cols-3 gap-4 mb-8">
        <article className="rounded-xl border border-muted bg-surface p-5">
          <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-2">Recommended Learning Path</h3>
          <p className="text-sm text-gray-600 dark:text-gray-400 leading-relaxed">
            Follow sections in order. Each page is designed to prepare the next one, so you avoid missing env,
            middleware, or deployment prerequisites.
          </p>
        </article>
        <article className="rounded-xl border border-muted bg-surface p-5">
          <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-2">Code-First Explanations</h3>
          <p className="text-sm text-gray-600 dark:text-gray-400 leading-relaxed">
            Examples are intentionally complete, not pseudo-code. You can use snippets directly in your project,
            then incrementally adapt naming and structure.
          </p>
        </article>
        <article className="rounded-xl border border-muted bg-surface p-5">
          <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-2">Production Mindset</h3>
          <p className="text-sm text-gray-600 dark:text-gray-400 leading-relaxed">
            Docs prioritize safe defaults: explicit CORS origin, strong JWT secret, controlled error exposure,
            health probes, and release reproducibility.
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
    </>
  );
}
