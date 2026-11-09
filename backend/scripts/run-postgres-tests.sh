#!/usr/bin/env bash
set -e

# Run Real Postgres integration tests using Docker Compose or fallback to local Node Postgres engine
if command -v docker >/dev/null 2>&1 && docker compose version >/dev/null 2>&1; then
  echo "Docker Compose detected — spinning up postgres:15-alpine service container..."
  docker compose -f docker-compose.test.yml up --build --abort-on-container-exit --exit-code-from test-runner
  docker compose -f docker-compose.test.yml down -v
else
  echo "Docker not detected — running Real PostgreSQL test suite directly via in-process engine..."
  cd "$(dirname "$0")/.."
  node test-real-postgres.js
  npm run audit:purge -- --live
fi
