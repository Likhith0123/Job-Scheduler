#!/usr/bin/env bash
set -euo pipefail

AUTH_URL="${AUTH_URL:-http://localhost:8081}"
GATEWAY_URL="${GATEWAY_URL:-http://localhost:8080}"

echo "Creating demo tenant..."
TENANT_JSON=$(curl -sf -X POST "$AUTH_URL/api/auth/tenants" \
  -H "Content-Type: application/json" \
  -d '{"name":"demo-tenant","rateLimitPerMinute":120}')
TENANT_ID=$(echo "$TENANT_JSON" | python3 -c "import sys,json; print(json.load(sys.stdin)['id'])")

echo "Creating API key for tenant $TENANT_ID..."
KEY_JSON=$(curl -sf -X POST "$AUTH_URL/api/auth/keys" \
  -H "Content-Type: application/json" \
  -d "{\"tenantId\":\"$TENANT_ID\",\"label\":\"demo\"}")
API_KEY=$(echo "$KEY_JSON" | python3 -c "import sys,json; print(json.load(sys.stdin)['apiKey'])")

echo "Creating sample job (runs every minute)..."
JOB_JSON=$(curl -sf -X POST "$GATEWAY_URL/api/jobs" \
  -H "Content-Type: application/json" \
  -H "X-API-Key: $API_KEY" \
  -d '{"name":"hello-job","cronExpression":"0 * * * * *","payload":{"message":"Hello from ChronoFlow"}}')
JOB_ID=$(echo "$JOB_JSON" | python3 -c "import sys,json; print(json.load(sys.stdin)['id'])")

echo ""
echo "Demo setup complete."
echo "  Tenant ID: $TENANT_ID"
echo "  API Key:   $API_KEY"
echo "  Job ID:    $JOB_ID"
echo ""
echo "List jobs:"
echo "  curl -H 'X-API-Key: $API_KEY' $GATEWAY_URL/api/jobs"
echo ""
echo "View runs (after scheduler fires):"
echo "  curl -H 'X-API-Key: $API_KEY' $GATEWAY_URL/api/jobs/$JOB_ID/runs"
