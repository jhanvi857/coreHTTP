# coreHTTP Operations Runbook

## Setup & Local Development
1. **Prerequisites**: Java 17+, Maven 3.9+. PostgreSQL 15+ only if enabling database features.
2. **Build**: `mvn clean compile`
3. **Run**: `mvn exec:java -Dexec.mainClass=com.jhanvi857.coreHTTP.server.HttpServer`
4. **Environment Variables** (see README for full list):
   - `COREHTTP_ENABLE_DB`: Set to `true` to enable PostgreSQL (default: `false`).
   - `DB_PASS`: Required when DB is enabled. No default.
   - `JDBC_URL`: Default `jdbc:postgresql://localhost:5432/corehttp`.
   - `COREHTTP_THREADS`: Default `10`.
   - `COREHTTP_QUEUE_CAPACITY`: Default `100`.

## Docker Deployment
1. **Launch Stack**: `DB_PASS=yourpassword docker-compose up -d`
2. **Verify App**: `curl http://localhost:8080/_health`
3. **Verify DB**: `docker-compose exec db psql -U postgres -d corehttp`

## Observability
- **Health**: `GET /_health` — returns JSON with server status, DB connectivity, and memory usage.
- **Metrics**: `GET /metrics` — returns Prometheus-compatible counters (request totals, error totals, per-path latency).
- **Logs**: Plain-text structured logs to STDOUT via Logback. Format: `timestamp [thread] level logger - message`.

## Security Notes
- **Auth utilities exist but are not route-integrated.** `JwtProvider` and `AuthMiddleware` are available as building blocks. No routes currently require authentication.
- **CORS**: Configurable via `COREHTTP_CORS_ORIGIN` env var. Defaults to `http://localhost:3000`.
- **Rate limiting**: Per-client (via `X-Forwarded-For` header), 100 requests per 10-second window by default.
- **No TLS**: Use a reverse proxy for HTTPS termination.
