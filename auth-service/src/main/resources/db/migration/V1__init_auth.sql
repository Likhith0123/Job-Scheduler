CREATE SCHEMA IF NOT EXISTS auth;

CREATE TABLE auth.tenants (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    rate_limit_per_minute INT NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE auth.api_keys (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES auth.tenants(id),
    label VARCHAR(255) NOT NULL,
    key_hash VARCHAR(64) NOT NULL UNIQUE,
    key_prefix VARCHAR(32) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_api_keys_tenant ON auth.api_keys(tenant_id);
