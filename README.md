# ChronoFlow

Distributed job scheduling platform built with Spring Boot, PostgreSQL, Redis, Kafka, Docker, Kubernetes, and OpenTelemetry.

## Architecture

```
Client → API Gateway → Auth / Job services
              ↓
         Redis (rate limits)

Scheduler → polls Job service → publishes Kafka (job.due)
Executor  → consumes Kafka    → records runs in Job service
```

| Service | Port (local) | Responsibility |
|---------|--------------|----------------|
| gateway-service | 8080 | Routing, API key auth, per-tenant rate limiting |
| auth-service | 8081 | Tenants and database-backed API keys |
| job-service | 8082 | Job definitions and execution history |
| scheduler-service | 8083 | Cron polling and Kafka dispatch |
| executor-service | 8084 | Job execution workers |

## Quick start (Docker)

```bash
chmod +x scripts/*.sh
./scripts/up.sh
./scripts/demo.sh
```

Open the React UI at **http://localhost:3001** and paste your API key, or use **Setup** to create a tenant.

### Frontend (local dev)

```bash
cd frontend
npm install
npm run dev
```

Runs at http://localhost:5173 (requires backend on ports 8080/8081).

Observability UIs:

- Grafana: http://localhost:3000 (admin / admin)
- Prometheus: http://localhost:9090
- Jaeger: http://localhost:16686

## API usage

Bootstrap a tenant and key directly against auth-service (admin setup):

```bash
curl -X POST http://localhost:8081/api/auth/tenants \
  -H 'Content-Type: application/json' \
  -d '{"name":"acme","rateLimitPerMinute":60}'

curl -X POST http://localhost:8081/api/auth/keys \
  -H 'Content-Type: application/json' \
  -d '{"tenantId":"<tenant-uuid>","label":"prod"}'
```

Create and manage jobs through the gateway with `X-API-Key`:

```bash
curl -X POST http://localhost:8080/api/jobs \
  -H 'Content-Type: application/json' \
  -H 'X-API-Key: cfk_...' \
  -d '{
    "name": "nightly-sync",
    "cronExpression": "0 0 2 * * *",
    "payload": {"target": "warehouse"}
  }'
```

Spring cron format is used (`sec min hour day month weekday`).

## Local development (without Docker apps)

Start infrastructure only:

```bash
docker compose up -d postgres redis kafka jaeger prometheus grafana
```

Run services from the repo root:

```bash
mvn -pl auth-service spring-boot:run
mvn -pl job-service spring-boot:run
mvn -pl scheduler-service spring-boot:run
mvn -pl executor-service spring-boot:run
mvn -pl gateway-service spring-boot:run
```

Build everything:

```bash
mvn clean package
```

## Kubernetes

Manifests under `k8s/` assume images are tagged locally as `chronoflow/<service>:1.0.0`.

```bash
docker build --build-arg MODULE=auth-service -t chronoflow/auth-service:1.0.0 .
# repeat for other services

kubectl apply -f k8s/
```

## Features

- **Multi-tenant API keys** stored as SHA-256 hashes in PostgreSQL
- **Per-tenant Redis rate limiting** at the gateway
- **Kafka-based dispatch** between scheduler and executor
- **OpenTelemetry traces** exported to Jaeger
- **Prometheus metrics** from Spring Actuator on every service
