import { CodeBlock, H2, P } from "../_components";

export default function DeploymentPage() {
  return (
    <>
      <h1 className="text-3xl md:text-4xl font-bold tracking-tight mb-4 text-gray-900 dark:text-white">Operations + Deployment</h1>
      <P>
        Deploy NioFlow apps with predictable startup behavior, observable health, and safe runtime defaults. This
        page covers both release mechanics and day-2 operations basics.
      </P>

      <H2 id="deployment-strategy">Deployment Strategy</H2>
      <P>
        Treat deployment as a repeatable process, not a manual push. Build immutable artifacts, configure env values
        per environment, verify readiness endpoints, and only then shift traffic.
      </P>
      <CodeBlock
        title="release-flow"
        language="text"
        code={`1) Build once (CI) and publish artifact
2) Promote same artifact to staging/prod
3) Inject environment-specific variables at runtime
4) Validate /_ready before accepting traffic
5) Roll forward or roll back based on health + logs`}
      />

      <H2 id="environment-variables">Required Environment Variables</H2>
      <P>
        Keep environment values outside source control. Use your platform secret manager for all sensitive values.
      </P>
      <CodeBlock
        title="required-env"
        language="bash"
        code={`PORT=8080
JWT_SECRET=replace-with-32-plus-char-secret
NIOFLOW_CORS_ORIGIN=https://your-frontend-domain
NIOFLOW_ENABLE_DB=false`}
      />

      <H2 id="recommended-production-env">Recommended Production Runtime Values</H2>
      <CodeBlock
        title="prod-runtime-env"
        language="bash"
        code={`NIOFLOW_EXPOSE_ERROR_DETAILS=false
NIOFLOW_DISABLE_AUTH=false
JAVA_TOOL_OPTIONS=-Xms256m -Xmx512m -XX:+UseG1GC`}
      />

      <H2 id="docker-deploy">Docker Deployment</H2>
      <P>
        Container deployments are ideal for consistency. Ensure the image runs as non-root and only exposes the
        required port.
      </P>
      <CodeBlock
        title="docker-build-run"
        language="bash"
        code={`docker build -t nioflow-app .
docker run --rm -p 8080:8080 \
  -e PORT=8080 \
  -e JWT_SECRET=replace-with-32-plus-char-secret \
  -e NIOFLOW_ENABLE_DB=false \
  nioflow-app`}
      />

      <H2 id="render-railway">Render / Railway Quick Setup</H2>
      <P>
        These platforms provide port and process orchestration. Keep startup command simple and rely on environment
        variables for all deployment-specific behavior.
      </P>
      <CodeBlock
        title="cloud-vars"
        language="text"
        code={`PORT = (auto provided by platform)
JWT_SECRET = your long random secret
NIOFLOW_CORS_ORIGIN = https://your-frontend-domain
NIOFLOW_ENABLE_DB = false`}
      />

      <H2 id="health-readiness">Health + Readiness Checks</H2>
      <P>
        Health indicates the process is alive, readiness indicates the service can accept production traffic.
      </P>
      <CodeBlock
        title="probe-endpoints"
        language="bash"
        code={`curl -i http://localhost:8080/_health
curl -i http://localhost:8080/_ready
curl -i http://localhost:8080/metrics`}
      />

      <H2 id="incident-basics">Operations Incident Basics</H2>
      <CodeBlock
        title="incident-checklist"
        language="text"
        code={`1) Confirm failing endpoint and scope
2) Check /_health and /_ready status
3) Review application logs around the failure window
4) Verify env values and secrets are present
5) Roll back to last healthy release if needed
6) Capture root cause and preventive action in runbook`}
      />

      <H2 id="post-deploy-verification">Post-Deploy Verification</H2>
      <CodeBlock
        title="post-deploy-smoke"
        language="bash"
        code={`curl -fsS https://your-api/_health
curl -fsS https://your-api/_ready
curl -fsS https://your-api/api/tasks/

# Expected: all commands return successful HTTP responses`}
      />

      <H2 id="production-checklist">Production Checklist</H2>
      <CodeBlock
        title="go-live"
        language="text"
        code={`[ ] JWT secret >= 32 chars
[ ] CORS origin locked to frontend domain
[ ] Error details disabled
[ ] Health/readiness checks configured
[ ] Logs and alerts enabled
[ ] Backup strategy documented`}
      />
    </>
  );
}
