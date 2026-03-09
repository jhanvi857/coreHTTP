#!/bin/bash
set -e

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

cd "$PROJECT_ROOT"

echo "Building project with Maven..."
mvn clean compile -q

echo "Starting CoreHTTP Server..."
mvn exec:java -Dexec.mainClass="com.jhanvi857.coreHTTP.server.HttpServer"