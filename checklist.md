# PHASE 1 - TCP SERVER LAYER

##  server/HttpServer.java

Purpose: Own the TCP socket lifecycle

Responsibilities:

- Create a ServerSocket
- Bind to the port
- Listen forever
- Accept client connections
- Delegate each connection

###  Design rules:
- This class does NOT know HTTP
- It only knows TCP connections
- It must not block other clients

## Outcome:

- connect using:
- telnet localhost 8080
- Connection succeeds (even if no response yet)

---
<br>

# PHASE 2 - CLIENT CONNECTION HANDLING
 
 ## server/ConnectionHandler.java

Purpose: Handle one client connection end-to-end

## Responsibilities:
- Read raw bytes from socket
- Detect client disconnect
- Pass raw data to HTTP parser
- Get a response
- Write response bytes
- Close connection safely

### Important rules:
- One instance = one client
- Errors here must not crash the server
- All exceptions are handled locally

## This is where:
- TCP becomes HTTP
- Bytes become meaning

---
<br>

# PHASE 3 - HTTP REQUEST PARSING
 ## protocol/HttpParser.java

### Purpose: Convert raw bytes → structured request

### What it must do:

- Read request line
(METHOD PATH HTTP/1.1)
- Parse headers
- Detect malformed requests
- Throw HttpParseException on invalid input

## What it must NOT do:

- Routing
- Business logic
- Response creation

##  protocol/HttpRequest.java

### Purpose: Immutable request model

It represents:

- HTTP method
- Path
- HTTP version
- Headers
- Body (optional)

### Rule:

- No logic
- Just data

---
<br>

# PHASE 4 - HTTP RESPONSE MODELING
 ## protocol/HttpResponse.java

### Purpose: Represent server response

It should hold:

- Status code

- Status message

- Headers

- Body

## This class answers:

“What exactly are we sending back?”

 ## protocol/HttpStatus.java

### Purpose: Central source of truth for status codes

Include:
- 200 OK
- 400 Bad Request
- 404 Not Found
- 500 Internal Server Error

## Why this matters:

- Prevents magic numbers

- Makes response construction clean

# PHASE 5 - ROUTING LAYER
 ## routing/Router.java

### Purpose: Decide which logic handles which request

Responsibilities:

- Register routes (method + path)

- Find matching handler

- Return handler or “not found”

### Think of it as:

A mini version of Spring’s @RequestMapping

 ## routing/RouteHandler.java

### Purpose: Define how routes behave

Responsibility:

- Take HttpRequest
- Return HttpResponse

This separation lets you:

- Add routes without touching server code
- Replace handlers easily

---
<br>

# PHASE 6 - STATIC FILE SERVING
 ## server/StaticFileHandler.java

### Purpose: Serve files from resources/public

Responsibilities:

- Map URL path → file path

- Check if file exists

- Read file contents

- Determine content type

- Return proper response

### Supported examples:

/ → index.html

/hello.txt → hello.txt

### Why this matters:

This is how real web servers started.

 ## resources/public/

Purpose: Public web root

Rules:

- Only static files here

- No Java logic
- Everything inside is directly accessible via URL

---
<br>

# PHASE 7 - ERROR HANDLING
 ## exception/HttpParseException.java

Used when:

- Request line is malformed

- Headers are invalid

- HTTP version unsupported

 ## exception/ClientDisconnectedException.java

Used when:

- Client closes connection unexpectedly

- Partial request is received

### Why separate exceptions?

So you can:

- Respond with correct status

- Avoid logging noise

- Keep server stable

---
<br>

# PHASE 8 - REQUEST LIFECYCLE (VERY IMPORTANT)

Once everything is wired, the flow must be exactly this:
```
Client connects
↓
HttpServer accepts socket
↓
ConnectionHandler reads bytes
↓
HttpParser creates HttpRequest
↓
Router selects RouteHandler
↓
Handler returns HttpResponse
↓
Response serialized to bytes
↓
Bytes written to socket
↓
Connection closed
```

---
<br>

# PHASE 9 - CONCURRENCY (REAL-WORLD STEP)
## Where:

- In HttpServer

## What:

- Each connection runs independently

- Use threads or thread pool

## Why:

- Browsers open multiple connections

- One slow client must not block others

---
<br>

# PHASE 10 - RUN SCRIPT
 ## scripts/run.sh

### Purpose: One-command server start

Responsibilities:

- Compile project

- Run main class

- Print startup message

- This shows engineering maturity.

---
<br>

# PHASE 11 - README 

README must explain:

- What coreHTTP is

- Why I built it

- How HTTP works internally

- Request lifecycle

- How to run it

- What I learned