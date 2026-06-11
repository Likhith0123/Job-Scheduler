#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

echo "Starting ChronoFlow infrastructure and services..."
docker compose up -d --build

echo ""
echo "Waiting for gateway health..."
for _ in $(seq 1 60); do
  if curl -sf http://localhost:8080/actuator/health >/dev/null 2>&1; then
    echo "Gateway is healthy."
    break
  fi
  sleep 2
done

echo ""
echo "ChronoFlow is up:"
echo "  Frontend UI:  http://localhost:3001"
echo "  API Gateway:  http://localhost:8080"
echo "  Auth (admin): http://localhost:8081"
echo "  Grafana:      http://localhost:3000  (admin / admin)"
echo "  Prometheus:   http://localhost:9090"
echo "  Jaeger:       http://localhost:16686"
echo ""
echo "Run ./scripts/demo.sh to create a tenant, API key, and sample job."
