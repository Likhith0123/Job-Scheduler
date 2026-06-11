CREATE SCHEMA IF NOT EXISTS jobs;

CREATE TABLE jobs.jobs (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    cron_expression VARCHAR(100) NOT NULL,
    payload JSONB NOT NULL,
    status VARCHAR(32) NOT NULL,
    next_run_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE jobs.job_runs (
    id UUID PRIMARY KEY,
    job_id UUID NOT NULL REFERENCES jobs.jobs(id),
    tenant_id UUID NOT NULL,
    status VARCHAR(32) NOT NULL,
    result TEXT,
    started_at TIMESTAMPTZ NOT NULL,
    completed_at TIMESTAMPTZ
);

CREATE INDEX idx_jobs_tenant ON jobs.jobs(tenant_id);
CREATE INDEX idx_jobs_due ON jobs.jobs(status, next_run_at);
CREATE INDEX idx_job_runs_job ON jobs.job_runs(job_id);
