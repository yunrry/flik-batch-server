package yunrry.flik.batch.domain;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class BatchExecutionLog {
    private Long id;
    private String jobName;
    private LocalDate executionDate;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;
    private int totalCount;
    private int successCount;
    private int failCount;
    private int skipCount;
    private int apiCallCount;
    private String errorMessage;
    private String executionParams;
}