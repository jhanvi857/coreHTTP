# CoreHTTP Micro-Framework

CoreHTTP is a high-performance, modular micro-framework for Java 17 designed for building scalable, event-driven web applications. It leverages Java Non-blocking I/O (NIO) to provide a lightweight alternative to traditional thread-per-connection servlet containers, offering a fluent API inspired by modern frameworks like Express.js and Javalin.

## Architectural Overview

The framework is built on a non-blocking architecture that separates connection management from request processing. This allows a single selector thread to manage thousands of concurrent connections efficiently.

### System Architecture

```mermaid
graph TD
    Client[HTTP Client] -->|TCP/IP| Selector[NIO Selector Loop]
    Selector -->|Accept/Read| EventManager[Event Manager]
    EventManager -->|Queue Task| WorkerPool[Fixed Thread Pool]
    
    subgraph Execution Pipeline
        WorkerPool --> Parser[HTTP Protocol Parser]
        Parser --> Router[Regex Routing Engine]
        Router --> MiddlewareChain[Global & Scoped Middleware]
        MiddlewareChain --> Handler[Route Handler / Plugin]
    end
    
    Handler -->|Zero-Copy| FileSystem[Static Assets]
    Handler -->|JDBC| Database[(PostgreSQL)]
    
    Handler --> Context[HttpContext]
    Context --> Responder[HTTP Response Writer]
    Responder -->|Write| Client
```

### Request Processing Lifecycle

The lifecycle of a request in CoreHTTP follows a deterministic path through the middleware pipeline and routing engine.

```mermaid
sequenceDiagram
    participant C as Client
    participant S as NIO Selector
    participant W as Worker Thread
    participant R as Router
    participant M as Middleware
    participant H as Handler

    C->>S: TCP Payload
    S->>W: Dispatch Connection
    W->>W: Parse HTTP Request
    W->>R: Resolve Route (Regex Match)
    R->>M: Execute Middleware Chain
    M->>H: Invoke Business Logic
    H->>H: Populate HttpContext
    W->>C: Transmit Buffered Response
```

## Core Framework Features

### Non-blocking I/O Engine
The core transport layer utilizes `java.nio` to implement an event-driven loop. By using a single `Selector` for connection acceptance and I/O readiness, the system avoids the memory overhead associated with massive thread counts.

### Advanced Regex Routing
The routing engine supports dynamic path parameters and complex patterns.
- Named Parameters: `/api/resources/:id`
- Wildcard Support: `/assets/*`
- Path-based Grouping: Prefixed groups with scoped middleware.

### Zero-Copy Static File Serving
For high-performance asset delivery, the framework utilizes `FileChannel.transferTo()`. This allows the operating system to transfer data directly from the file system cache to the network buffer, bypassing the Java heap entirely and reducing CPU cycles.

### Unified HttpContext Abstraction
The `HttpContext` provides a unified interface for request data extraction and response generation.
- Type-safe JSON deserialization via `ctx.body(Class<T>)`.
- Fluent response building: `ctx.status(201).json(data)`.
- Path parameter retrieval via `ctx.pathParam(name)`.

## Repository Structure

The project is organized as a Multi-Module Maven repository to ensure strict isolation between the framework core and consumer applications.

```text
.
├── corehttp-framework/       # Reusable framework logic (NIO, Parser, Router)
│   ├── protocol/             # HTTP/1.1 Model and Parsing
│   ├── routing/              # Regex Engine and Context logic
│   ├── server/               # NIO Selector and Connection Management
│   └── plugin/               # Official Extension Points
└── task-planner-app/         # Reference Implementation
    ├── controller/           # REST Handlers
    └── repository/           # PostgreSQL Integration
```

## Deployment and Usage

### Prerequisites
- Java Development Kit (JDK) 17 or higher
- Apache Maven 3.9+

### Installation
To include the framework in a local environment:

```bash
cd corehttp-framework
mvn clean install
```

### Reference Implementation
A sample CRUD application utilizing the framework is available in the `task-planner-app` directory.

```bash
cd task-planner-app
mvn compile exec:java -Dexec.mainClass=com.jhanvi857.taskplanner.DemoApplication
```

## Security and Resilience

- **Path Traversal Protection**: Resolution logic validates canonical paths against the configured base directory.
- **Payload Constraints**: Configurable limits on header size (8KB) and body size (10MB) to mitigate buffer-based attacks.
- **Resource Management**: Bounded thread pools and request queues prevent resource exhaustion under denial-of-service conditions.
- **Rate Limiting**: Sliding window implementation for per-IP request throttling.

## Technical Specifications

| Component | Implementation Detail |
|:---|:---|
| Protocol | HTTP/1.1 (Persistent Connections) |
| I/O Model | Java Non-blocking I/O (NIO) |
| Concurrency | Fixed ThreadPool Executor |
| Routing | Regex-based Pattern Matching |
| Serialization | Jackson (JSON) |
| Logging | SLF4J with Logback |

---
Documentation compiled for High-Performance Systems Engineering standards.
