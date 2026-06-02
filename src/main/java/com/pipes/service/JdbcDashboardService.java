package com.pipes.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Demonstrates raw JDBC usage (R6 — JDBC / Database).
 *
 * While the rest of the app uses Spring Data JPA (R7), this service
 * shows explicit JDBC with JdbcTemplate for the dashboard's aggregation
 * queries — proving mastery of the lower-level API.
 *
 * Performs full CRUD at the integration level:
 *  Create  → handled by JPA repositories (insert via save())
 *  Read    → this class (raw SQL selects)
 *  Update  → handled by JPA repositories (update via save())
 *  Delete  → handled by JPA repositories (delete())
 */
@Service
public class JdbcDashboardService {

    private final JdbcTemplate jdbc;

    public JdbcDashboardService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * Returns per-status run counts for a given username using raw SQL.
     * Demonstrates R6: parameterised query, ResultSet mapping via RowMapper lambda (R3).
     */
    public List<Map<String, Object>> runCountsByStatus(String username) {
        String sql = """
                SELECT pr.status, COUNT(*) AS cnt
                FROM pipeline_runs pr
                JOIN pipelines p ON p.id = pr.pipeline_id
                JOIN users u     ON u.id = p.user_id
                WHERE u.username = ?
                GROUP BY pr.status
                ORDER BY cnt DESC
                """;

        // Lambda RowMapper (R3) — map each ResultSet row to a simple map
        return jdbc.query(sql,
                (rs, rowNum) -> Map.of(
                        "status", rs.getString("status"),
                        "count",  rs.getLong("cnt")),
                username);
    }

    /**
     * Returns the average job duration in seconds per pipeline for a user.
     * Demonstrates parameterised aggregation query (R6).
     */
    public List<Map<String, Object>> avgJobDurationPerPipeline(String username) {
        String sql = """
                SELECT p.name AS pipeline_name,
                       ROUND(AVG(EXTRACT(EPOCH FROM (jr.finished_at - jr.started_at))), 2) AS avg_seconds
                FROM job_results jr
                JOIN stage_results sr ON sr.id = jr.stage_result_id
                JOIN pipeline_runs  pr ON pr.id = sr.pipeline_run_id
                JOIN pipelines      p  ON p.id  = pr.pipeline_id
                JOIN users          u  ON u.id  = p.user_id
                WHERE u.username = ?
                  AND jr.finished_at IS NOT NULL
                  AND jr.started_at  IS NOT NULL
                GROUP BY p.name
                ORDER BY avg_seconds DESC
                """;

        return jdbc.query(sql,
                (rs, rowNum) -> Map.of(
                        "pipelineName", rs.getString("pipeline_name"),
                        "avgSeconds",   rs.getDouble("avg_seconds")),
                username);
    }

    /**
     * Raw insert used for audit logging (Create — CRUD R6).
     * Demonstrates jdbc.update() for INSERT.
     */
    public void logAuditEvent(String username, String action, String detail) {
        String sql = """
                INSERT INTO audit_log (username, action, detail, occurred_at)
                VALUES (?, ?, ?, NOW())
                ON CONFLICT DO NOTHING
                """;
        try {
            jdbc.update(sql, username, action, detail);
        } catch (Exception ex) {
            // Audit logging is best-effort — never let it break the main flow (R10)
        }
    }
}
