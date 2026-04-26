# NioFlow Performance Benchmarks

This document tracks the performance characteristics of the NioFlow framework under various load conditions.

## Load Test Results

Tests are conducted using `k6` against the `task-planner-app` reference implementation.

| Endpoint | VUs | p50 (ms) | p95 (ms) | p99 (ms) | RPS | Error % | Date |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| `GET /api/tasks/` | 20 | | | | | | 2026-04-26 |
| `GET /api/tasks/` | 100 | | | | | | 2026-04-26 |
| `GET /_health` | 200 | | | | | | 2026-04-26 |
| `GET /rate-limited`| 10 | | | | | | 2026-04-26 |

## Scenario Analysis

### 1. Steady State (20 VUs)
Targeting the median latency and throughput for typical small-to-medium service loads.

### 2. High Concurrency (100+ VUs)
Testing the limits of the bounded worker pool and the efficiency of the NIO selector loop.

### 3. Rate Limiting Protection
Demonstrating the 429 "Too Many Requests" behavior when individual clients exceed their allocated quota.

## Environment Details
- **OS:** Windows / Linux (Docker)
- **CPU:** [Insert CPU]
- **RAM:** [Insert RAM]
- **Java Version:** OpenJDK 17
- **Database:** PostgreSQL 15 (if applicable)
