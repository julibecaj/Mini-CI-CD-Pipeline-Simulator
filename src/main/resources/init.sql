-- Pipes — PostgreSQL initialisation script
-- Run once after the JPA schema is auto-created (ddl-auto=update).
-- The main tables (users, pipelines, stages, jobs, pipeline_runs,
-- stage_results, job_results) are created automatically by Hibernate.

-- ── Audit log table (used by JdbcDashboardService — R6) ──────────────────────
CREATE TABLE IF NOT EXISTS audit_log (
    id          BIGSERIAL PRIMARY KEY,
    username    VARCHAR(60)  NOT NULL,
    action      VARCHAR(100) NOT NULL,
    detail      TEXT,
    occurred_at TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_audit_username ON audit_log(username);
CREATE INDEX IF NOT EXISTS idx_audit_occurred ON audit_log(occurred_at DESC);

-- ── Useful indexes on the main tables ─────────────────────────────────────────
CREATE INDEX IF NOT EXISTS idx_pipeline_runs_pipeline ON pipeline_runs(pipeline_id);
CREATE INDEX IF NOT EXISTS idx_pipeline_runs_status   ON pipeline_runs(status);
CREATE INDEX IF NOT EXISTS idx_stage_results_run      ON stage_results(pipeline_run_id);
CREATE INDEX IF NOT EXISTS idx_job_results_stage      ON job_results(stage_result_id);
