export type Feature = {
  title: string;
  description: string;
};

export type Endpoint = {
  method: "GET" | "POST" | "PUT" | "DELETE";
  path: string;
  purpose: string;
};

export type ConfigRow = {
  purpose: string;
  jvmProperty: string;
  envVar: string;
  defaultValue: string;
};

export type Milestone = {
  phase: string;
  goal: string;
  deliverables: string[];
};

export const features: Feature[] = [
  {
    title: "NIO Event Loop",
    description: "Highly scalable, non-blocking I/O using Java's Selector API for thousands of connections.",
  },
  {
    title: "JWT Authentication",
    description: "Built-in stateless authentication with secure password hashing via BCrypt.",
  },
  {
    title: "SQL Persistence",
    description: "Native PostgreSQL support with HikariCP connection pooling and schema migrations.",
  },
  {
    title: "Middleware Engine",
    description: "Chain-of-responsibility pattern for logging, CORS, rate limiting, and observability.",
  },
];

export const endpoints: Endpoint[] = [
  { method: "GET", path: "/", purpose: "Serve static assets" },
  { method: "GET", path: "/_health", purpose: "Component health status" },
  { method: "GET", path: "/metrics", purpose: "Prometheus observability data" },
  { method: "GET", path: "/api/tasks", purpose: "List all tasks" },
  { method: "POST", path: "/api/tasks", purpose: "Create a new task" },
  { method: "DELETE", path: "/api/tasks/{id}", purpose: "Remove a task" },
];

export const configRows: ConfigRow[] = [
  {
    purpose: "Static file root",
    jvmProperty: "corehttp.staticDir",
    envVar: "COREHTTP_STATIC_DIR",
    defaultValue: "auto-resolved",
  },
  {
    purpose: "JDBC Connection URL",
    jvmProperty: "jdbc.url",
    envVar: "JDBC_URL",
    defaultValue: "jdbc:postgresql://localhost:5432/corehttp",
  },
  {
    purpose: "Worker threads",
    jvmProperty: "corehttp.threads",
    envVar: "COREHTTP_THREADS",
    defaultValue: "10",
  },
  {
    purpose: "Rate Limit Count",
    jvmProperty: "ratelimit.max",
    envVar: "RATE_LIMIT_MAX",
    defaultValue: "100",
  },
];

export const roadmapMilestones: Milestone[] = [
  {
    phase: "Phase 1 - COMPLETED",
    goal: "Core NIO Engine",
    deliverables: [
      "Selector-based event loop",
      "Request/Response protocol parsing",
      "Static file streaming",
    ],
  },
  {
    phase: "Phase 2 - COMPLETED",
    goal: "Framework Features",
    deliverables: [
      "Method-based routing",
      "Middleware chain system",
      "JWT & BCrypt security",
    ],
  },
  {
    phase: "Phase 3 - COMPLETED",
    goal: "Production Grade",
    deliverables: [
      "PostgreSQL persistence",
      "Prometheus metrics & logging",
      "Docker/Compose orchestration",
    ],
  },
];
