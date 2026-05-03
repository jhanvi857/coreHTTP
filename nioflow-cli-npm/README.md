# NioFlow CLI

The official command-line interface for the **NioFlow** HTTP framework. Scaffold new projects and run your applications with a single command.

## Prerequisites

- **Java 17+**: The framework and CLI run on the JVM.
- **Node.js**: Required only for the CLI installer.

## Installation

Install the CLI globally using npm:

```bash
npm install -g @jhanvi857/nioflow-cli
```

## Quick Start

### 1. Create a new project
```bash
nioflow new my-app
cd my-app
```

### 2. Run in development mode (with hot-reload)
```bash
nioflow dev
```

### 3. Run in production mode
```bash
nioflow run
```

## Available Commands

| Command | Description |
| :--- | :--- |
| `nioflow new <name>` | Scaffolds a new Java project with all dependencies configured. |
| `nioflow dev` | Compiles and runs the project with `NIOFLOW_WATCH=true`. |
| `nioflow run` | Compiles and runs the project in standard mode. |
| `nioflow help` | Shows usage information. |
| `nioflow --version` | Shows the current CLI version. |

## License

MIT
