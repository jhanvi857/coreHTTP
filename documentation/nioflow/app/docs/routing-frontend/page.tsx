import { CodeBlock, H2, P } from "../_components";

export default function RoutingFrontendPage() {
  return (
    <>
      <h1 className="text-3xl md:text-4xl font-bold tracking-tight mb-4 text-gray-900 dark:text-white">Routing + Frontend Integration</h1>
      <P>Build custom routes and return API responses that frontend clients can consume directly.</P>

      <H2 id="custom-routes">Custom Routes</H2>
      <CodeBlock
        title="routes"
        language="java"
        code={`app.get("/api/users/:id", ctx -> {
    String id = ctx.pathParam("id");
    ctx.json(java.util.Map.of("id", id, "name", "Demo User"));
});

app.post("/api/users", ctx -> {
    CreateUserReq req = ctx.body(CreateUserReq.class);
    if (req == null || req.getEmail() == null) {
        ctx.status(400).json(java.util.Map.of("error", "email is required"));
        return;
    }
    ctx.status(201).json(java.util.Map.of("message", "created"));
});`}
      />

      <H2 id="request-response">Request & Response Model</H2>
      <CodeBlock
        title="http-context"
        language="java"
        code={`app.post("/api/items/:id", ctx -> {
    String id = ctx.pathParam("id");
    String auth = ctx.header("Authorization");
    ItemReq req = ctx.body(ItemReq.class);

    if (req == null) {
        ctx.status(400).json(java.util.Map.of("error", "body required"));
        return;
    }

    ctx.header("X-Request-Id", "abc-123");
    ctx.status(200).json(java.util.Map.of(
        "id", id,
        "authorized", auth != null,
        "name", req.getName()
    ));
});`}
      />

      <H2 id="frontend-integration">Frontend fetch() Example</H2>
      <CodeBlock
        title="frontend-fetch"
        language="javascript"
        code={`const base = "https://your-api-domain";

export async function fetchTasks(token) {
  const res = await fetch(base + "/api/tasks/", {
    method: "GET",
    headers: {
      "Content-Type": "application/json",
      "Authorization": "Bearer " + token
    }
  });

  if (!res.ok) {
    const err = await res.json();
    throw new Error(err.error || "Request failed");
  }

  return res.json();
}`}
      />
    </>
  );
}
