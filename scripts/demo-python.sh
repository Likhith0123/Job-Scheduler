#!/usr/bin/env bash
set -euo pipefail

GATEWAY_URL="${GATEWAY_URL:-http://localhost:8080}"
API_KEY="${API_KEY:-}"

if [[ -z "$API_KEY" ]]; then
  echo "Usage: API_KEY=cfk_... ./scripts/demo-python.sh"
  echo "Or run ./scripts/demo.sh first and pass the printed API key."
  exit 1
fi

echo "Creating inline Python job..."
curl -sf -X POST "$GATEWAY_URL/api/jobs" \
  -H "Content-Type: application/json" \
  -H "X-API-Key: $API_KEY" \
  -d '{
    "name": "python-inline-job",
    "cronExpression": "0/30 * * * * *",
    "payload": {
      "type": "python",
      "code": "print(\"Hello from inline Python\")\nprint(40 + 2)"
    }
  }'

echo ""
echo "Creating module Python job..."
curl -sf -X POST "$GATEWAY_URL/api/jobs" \
  -H "Content-Type: application/json" \
  -H "X-API-Key: $API_KEY" \
  -d '{
    "name": "python-module-job",
    "cronExpression": "0 * * * * *",
    "payload": {
      "type": "python",
      "module": "scripts.hello"
    }
  }'

echo ""
echo "Python demo jobs created. Check runs in the UI or via:"
echo "  curl -H 'X-API-Key: $API_KEY' $GATEWAY_URL/api/jobs"
