// BatchController.java
package yunrry.flik.batch.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import yunrry.flik.batch.service.RateLimitService;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/batch")
@RequiredArgsConstructor
public class BatchController {

    private final JobLauncher jobLauncher;
    private final Job tourismDataJob;
    private final JobRepository jobRepository;
    private final RateLimitService rateLimitService;

    @PostMapping("/tourism/run")
    public ResponseEntity<Map<String, Object>> runTourismBatch() {
        Map<String, Object> response = new HashMap<>();

        try {
            // API 제한 확인
            if (!rateLimitService.canMakeRequest()) {
                response.put("success", false);
                response.put("message", "API rate limit exceeded");
                response.put("remainingCount", rateLimitService.getRemainingCount());
                return ResponseEntity.badRequest().body(response);
            }

            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("executionTime", LocalDateTime.now().toString())
                    .addString("triggerType", "MANUAL")
                    .toJobParameters();

            JobExecution jobExecution = jobLauncher.run(tourismDataJob, jobParameters);

            response.put("success", true);
            response.put("jobExecutionId", jobExecution.getId());
            response.put("status", jobExecution.getStatus().toString());
            response.put("startTime", jobExecution.getStartTime());
            response.put("apiCallsRemaining", rateLimitService.getRemainingCount());

            log.info("Tourism batch job started manually - Execution ID: {}", jobExecution.getId());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Failed to start tourism batch job", e);
            response.put("success", false);
            response.put("message", "Failed to start batch job: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/tourism/status/{jobExecutionId}")
    public ResponseEntity<Map<String, Object>> getBatchStatus(@PathVariable Long jobExecutionId) {
        Map<String, Object> response = new HashMap<>();

        try {
            JobExecution jobExecution = jobRepository.getLastJobExecution(tourismDataJob.getName(),
                    new JobParametersBuilder().addLong("jobExecutionId", jobExecutionId).toJobParameters());

            if (jobExecution == null) {
                response.put("success", false);
                response.put("message", "Job execution not found");
                return ResponseEntity.notFound().build();
            }

            response.put("success", true);
            response.put("jobExecutionId", jobExecution.getId());
            response.put("status", jobExecution.getStatus().toString());
            response.put("startTime", jobExecution.getStartTime());
            response.put("endTime", jobExecution.getEndTime());
            response.put("exitStatus", jobExecution.getExitStatus().getExitCode());

            // Step 정보 추가
            response.put("steps", jobExecution.getStepExecutions().stream()
                    .map(this::mapStepExecution)
                    .toList());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Failed to get batch status", e);
            response.put("success", false);
            response.put("message", "Failed to get status: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/tourism/rate-limit")
    public ResponseEntity<Map<String, Object>> getRateLimitStatus() {
        Map<String, Object> response = new HashMap<>();

        response.put("currentCount", rateLimitService.getCurrentCount());
        response.put("remainingCount", rateLimitService.getRemainingCount());
        response.put("dailyLimit", 1000);
        response.put("canMakeRequest", rateLimitService.canMakeRequest());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/tourism/stop/{jobExecutionId}")
    public ResponseEntity<Map<String, Object>> stopBatch(@PathVariable Long jobExecutionId) {
        Map<String, Object> response = new HashMap<>();

        try {
            // JobOperator를 통한 중지는 복잡하므로 간단한 응답만
            response.put("success", false);
            response.put("message", "Batch job stopping is not implemented. Job will complete current chunk.");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Failed to stop batch job", e);
            response.put("success", false);
            response.put("message", "Failed to stop job: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    private Map<String, Object> mapStepExecution(StepExecution stepExecution) {
        Map<String, Object> stepInfo = new HashMap<>();
        stepInfo.put("stepName", stepExecution.getStepName());
        stepInfo.put("status", stepExecution.getStatus().toString());
        stepInfo.put("readCount", stepExecution.getReadCount());
        stepInfo.put("writeCount", stepExecution.getWriteCount());
        stepInfo.put("skipCount", stepExecution.getSkipCount());
        stepInfo.put("startTime", stepExecution.getStartTime());
        stepInfo.put("endTime", stepExecution.getEndTime());
        stepInfo.put("exitStatus", stepExecution.getExitStatus().getExitCode());
        return stepInfo;
    }
}