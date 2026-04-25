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
    title: "Hybrid NIO Runtime",
    description: "Selector-based accept loop with bounded worker pool processing for predictable load behavior.",
  },
  {
    title: "Fluent Route Policies",
    description: "Per-route timeout, route-scoped rate limits, and request hedging through fluent registration APIs.",
  },
  {
    title: "Chaos + Replay (Opt-in)",
    description: "Controlled chaos injection and in-memory request replay guarded by explicit environment flags.",
  },
  {
    title: "Circuit Breaker Groups",
    description: "Route-group scoped circuit breaker with CLOSED/OPEN/HALF_OPEN states and metrics visibility.",
  },
];

export const endpoints: Endpoint[] = [
  { method: "GET", path: "/", purpose: "Serve static assets index" },
  { method: "GET", path: "/_health", purpose: "Component health status" },
  { method: "GET", path: "/_ready", purpose: "Dependency-aware readiness status" },
  { method: "GET", path: "/metrics", purpose: "Prometheus observability data" },
  { method: "GET", path: "/api/tasks/", purpose: "List tasks (protected by auth by default)" },
  { method: "POST", path: "/api/tasks/", purpose: "Create a task" },
  { method: "DELETE", path: "/api/tasks/:id", purpose: "Delete a task" },
];

export const configRows: ConfigRow[] = [
  {
    purpose: "Static file root",
    jvmProperty: "nioflow.staticDir",
    envVar: "NIOFLOW_STATIC_DIR",
    defaultValue: "auto-resolved",
  },
  {
    purpose: "JDBC Connection URL",
    jvmProperty: "-",
    envVar: "JDBC_URL",
    defaultValue: "jdbc:postgresql://localhost:5432/nioflow",
  },
  {
    purpose: "Worker threads",
    jvmProperty: "nioflow.threads",
    envVar: "NIOFLOW_THREADS",
    defaultValue: "64",
  },
  {
    purpose: "Worker queue capacity",
    jvmProperty: "nioflow.queueCapacity",
    envVar: "NIOFLOW_QUEUE_CAPACITY",
    defaultValue: "1000",
  },
  {
    purpose: "Chaos middleware guard",
    jvmProperty: "-",
    envVar: "NIOFLOW_CHAOS_ENABLED",
    defaultValue: "false",
  },
  {
    purpose: "Replay feature guard",
    jvmProperty: "-",
    envVar: "NIOFLOW_REPLAY_ENABLED",
    defaultValue: "false",
  },
];

export const roadmapMilestones: Milestone[] = [
  {
    phase: "Phase 1 - COMPLETED",
    goal: "Core Engine Mechanics",
    deliverables: [
      "NIO Selector-based TCP event loop",
      "Bounded Worker Thread Pool handoff",
      "Explicit HTTP/1.1 Request model",
    ],
  },
  {
    phase: "Phase 2 - COMPLETED",
    goal: "Framework Logistics",
    deliverables: [
      "Zero-Copy (DMA) static file serving natively",
      "Chain-of-responsibility Middleware pipelines",
      "Declarative regex routing and parameter extraction",
    ],
  },
  {
    phase: "Phase 3 - COMPLETED",
    goal: "Production Hardening",
    deliverables: [
      "Native HTTPS/TLS with SSLSocketFactory",
      "Asynchronous JDBC thread-offloading",
      "Global Error intercepts & Graceful Shutdowns",
    ],
  },
];
