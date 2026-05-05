import { CodeBlock, H2, P, Pagination } from "../_components";

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
    // SECURITY: Use pathParamAsLong or pathParamAsInt when passing to a database.
    // Raw pathParam("id") could contain SQL injection vectors or non-numeric garbage.
    long id = ctx.pathParamAsLong("id");
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

      <H2 id="query-parameters">Query Parameters</H2>
      <P>Extract query parameters natively. If a parameter is missing, it returns null.</P>
      <CodeBlock
        title="query-params"
        language="java"
        code={`// Example: /api/search?page=1&limit=10
app.get("/api/search", ctx -> {
    String page = ctx.queryParam("page");
    String limit = ctx.queryParam("limit");
    
    int pageNum = page != null ? Integer.parseInt(page) : 1;
    int limitNum = limit != null ? Integer.parseInt(limit) : 20;

    ctx.json(java.util.Map.of("page", pageNum, "limit", limitNum));
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

      <H2 id="routing-errors">404 Not Found vs 405 Method Not Allowed</H2>
      <P>NioFlow strictly distinguishes between a path that does not exist (404) and a path that exists but was called with the wrong HTTP method (405). This adheres to REST best practices and prevents confusing client errors.</P>
      <CodeBlock
        title="curl-tests"
        language="bash"
        code={`# 404 Not Found (path doesn't exist)
$ curl -i -X GET http://localhost:8080/does-not-exist
HTTP/1.1 404 Not Found

# 405 Method Not Allowed (path exists, wrong method)
$ curl -i -X POST http://localhost:8080/api/users/1
HTTP/1.1 405 Method Not Allowed`}
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
      <Pagination 
        prev={{ href: "/docs/getting-started", label: "Getting Started" }}
        next={{ href: "/docs/auth-security", label: "Auth + Security" }}
      />
    </>
  );
}
