package yunrry.flik.batch.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import yunrry.flik.batch.domain.BatchExecutionLog;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Repository
@RequiredArgsConstructor
public class BatchExecutionLogRepository {

    private final JdbcTemplate jdbcTemplate;

    public void logBatchStart(String jobName, LocalDate executionDate) {
        String sql = """
            INSERT INTO batch_execution_log (job_name, execution_date, start_time, status)
            VALUES (?, ?, ?, 'RUNNING')
            ON DUPLICATE KEY UPDATE
                start_time = VALUES(start_time),
                status = 'RUNNING',
                end_time = NULL,
                error_message = NULL
            """;

        jdbcTemplate.update(sql, jobName, executionDate, LocalDateTime.now());
    }

    public void logBatchEnd(String jobName, LocalDate executionDate, String status,
                            int totalCount, int successCount, int failCount,
                            int apiCallCount, String errorMessage) {
        String sql = """
            UPDATE batch_execution_log 
            SET end_time = ?, status = ?, total_count = ?, success_count = ?, 
                fail_count = ?, api_call_count = ?, error_message = ?
            WHERE job_name = ? AND execution_date = ?
            """;

        jdbcTemplate.update(sql, LocalDateTime.now(), status, totalCount,
                successCount, failCount, apiCallCount, errorMessage,
                jobName, executionDate);
    }

    public Integer getLastProcessedPage(String jobName, LocalDate executionDate) {
        String sql = """
            SELECT JSON_EXTRACT(execution_params, '$.lastPage') as lastPage
            FROM batch_execution_log 
            WHERE job_name = ? AND execution_date = ?
            """;

        try {
            return jdbcTemplate.queryForObject(sql, Integer.class, jobName, executionDate);
        } catch (Exception e) {
            return 0; // 첫 실행
        }
    }

    public void updateExecutionParams(String jobName, LocalDate executionDate,
                                      String paramsJson) {
        String sql = """
            UPDATE batch_execution_log 
            SET execution_params = ?
            WHERE job_name = ? AND execution_date = ?
            """;

        jdbcTemplate.update(sql, paramsJson, jobName, executionDate);
    }
}