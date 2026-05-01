# Contributing to NioFlow

First off, thanks for taking the time to contribute! 

The following is a set of guidelines for contributing to NioFlow and its packages. These are mostly guidelines, not rules. Use your best judgment, and feel free to propose changes to this document in a pull request.

## Development Setup

1. Ensure you have **Java 17+** and **Maven 3.9+** installed.
2. Clone the repository: `git clone https://github.com/jhanvi857/coreHTTP.git`
3. Copy `.env.example` to `.env` and configure local values.
4. Run tests: `./mvnw clean test` (or `.\mvn.ps1 clean test` on Windows)

## Submitting Pull Requests

1. **Fork the repo** and create your branch from `main`.
2. **Write tests** for any new features or bug fixes.
3. Ensure the test suite passes: `./mvnw clean test`.
4. Update documentation if necessary.
5. Create a pull request describing the changes and linking to any relevant issues.

## Code Style

- We follow standard Java conventions. 
- Try to keep explicit flow without relying on hidden reflection or heavy abstractions.

We appreciate all your contributions and feedback!
