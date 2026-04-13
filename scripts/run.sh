#!/bin/bash
set -e

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

cd "$PROJECT_ROOT"

echo "Building project with Maven..."
mvn clean compile -q

echo "Starting Task Planner App (Demo)..."
mvn -pl task-planner-app exec:java -Dexec.mainClass="io.github.jhanvi857.taskplanner.DemoApplication"
