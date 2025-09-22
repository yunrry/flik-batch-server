// BatchController.java
package yunrry.flik.batch.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import yunrry.flik.batch.job.reader.DetailItemReader;
import yunrry.flik.batch.job.reader.TourismApiItemReader;
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
    private final Job detailIntroOnlyJob;
    private final Job detailIntroOnlyJob2;
    private final Job labelDetailJob;
    private final Job labelDetailJob2;
    private final DetailItemReader detailItemReader;
    private final TourismApiItemReader tourismApiItemReader;

    @GetMapping("/test")
    public String test() {
        return "Controller working";
    }

    @PostMapping("/tourism/run")
    public ResponseEntity<Map<String, Object>> runTourismBatch(
            @RequestParam String areaCode,
            @RequestParam String serviceKey) {

        Map<String, Object> response = new HashMap<>();

        try {
            if (!rateLimitService.canMakeRequest()) {
                response.put("success", false);
                response.put("message", "API rate limit exceeded");
                response.put("remainingCount", rateLimitService.getRemainingCount());
                return ResponseEntity.badRequest().body(response);
            }

            // Reader에 지역코드와 서비스키 설정
            tourismApiItemReader.setAreaCode(areaCode);
            tourismApiItemReader.setServiceKey(serviceKey);

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


    @PostMapping("/tourism/detail-intro/run")
    public ResponseEntity<Map<String, Object>> runDetailIntroOnly() {
        Map<String, Object> response = new HashMap<>();

        try {
            // API 제한 확인
            if (!rateLimitService.canMakeRequest()) {
                response.put("success", false);
                response.put("message", "API rate limit exceeded");
                response.put("remainingCount", rateLimitService.getRemainingCount());
                return ResponseEntity.badRequest().body(response);
            }

            // *** Reader 상태 초기화 ***
            detailItemReader.reset();

            // JobParameters 생성
            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("executionTime", LocalDateTime.now().toString())
                    .addString("triggerType", "MANUAL_DETAIL_INTRO")
                    .toJobParameters();

            // 배치 Job 실행
            JobExecution jobExecution = jobLauncher.run(detailIntroOnlyJob, jobParameters);

            response.put("success", true);
            response.put("jobExecutionId", jobExecution.getId());
            response.put("status", jobExecution.getStatus().toString());
            response.put("startTime", jobExecution.getStartTime());
            response.put("apiCallsRemaining", rateLimitService.getRemainingCount());

            log.info("Detail intro batch job started manually - Execution ID: {}", jobExecution.getId());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Failed to start detail intro batch job", e);
            response.put("success", false);
            response.put("message", "Failed to start detail intro job: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PostMapping("/tourism/detail-intro2/run")
    public ResponseEntity<Map<String, Object>> runDetailIntroOnly2() {
        Map<String, Object> response = new HashMap<>();

        try {
            // API 제한 확인
//            if (!rateLimitService.canMakeRequest()) {
//                response.put("success", false);
//                response.put("message", "API rate limit exceeded");
//                response.put("remainingCount", rateLimitService.getRemainingCount());
//                return ResponseEntity.badRequest().body(response);
//            }

            // *** Reader 상태 초기화 ***
            detailItemReader.reset();

            // JobParameters 생성
            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("executionTime", LocalDateTime.now().toString())
                    .addString("triggerType", "MANUAL_DETAIL_INTRO")
                    .toJobParameters();

            // 배치 Job 실행
            JobExecution jobExecution = jobLauncher.run(detailIntroOnlyJob2, jobParameters);

            response.put("success", true);
            response.put("jobExecutionId", jobExecution.getId());
            response.put("status", jobExecution.getStatus().toString());
            response.put("startTime", jobExecution.getStartTime());
            response.put("apiCallsRemaining", rateLimitService.getRemainingCount());

            log.info("Detail intro batch job started manually - Execution ID: {}", jobExecution.getId());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Failed to start detail intro batch job", e);
            response.put("success", false);
            response.put("message", "Failed to start detail intro job: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }


    @GetMapping("/tourism/detail-intro/status/{jobExecutionId}")
    public ResponseEntity<Map<String, Object>> getDetailIntroStatus(@PathVariable Long jobExecutionId) {
        Map<String, Object> response = new HashMap<>();

        try {
            JobExecution jobExecution = jobRepository.getLastJobExecution(detailIntroOnlyJob.getName(),
                    new JobParametersBuilder().addLong("jobExecutionId", jobExecutionId).toJobParameters());

            if (jobExecution == null) {
                response.put("success", false);
                response.put("message", "Detail intro job execution not found");
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
            log.error("Failed to get detail intro2 batch status", e);
            response.put("success", false);
            response.put("message", "Failed to get status: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }



    @PostMapping("/label-detail")
    public ResponseEntity<Map<String, Object>> runLabelDetailJob() {
        try {
            // 고유한 JobParameters 생성 (중복 실행 방지)
            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("timestamp", LocalDateTime.now().toString())
                    .toJobParameters();

            // Job 실행
            var jobExecution = jobLauncher.run(labelDetailJob, jobParameters);

            log.info("Label detail job started with execution id: {}", jobExecution.getId());

            return ResponseEntity.ok(Map.of(
                    "status", "started",
                    "executionId", jobExecution.getId(),
                    "message", "Label detail job has been started successfully"
            ));

        } catch (Exception e) {
            log.error("Failed to start label detail job", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "status", "error",
                    "message", "Failed to start label detail job: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/label-detail2")
    public ResponseEntity<Map<String, Object>> runLabelDetailJob2() {
        try {
            // 고유한 JobParameters 생성 (중복 실행 방지)
            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("timestamp", LocalDateTime.now().toString())
                    .toJobParameters();

            // Job 실행
            var jobExecution = jobLauncher.run(labelDetailJob2, jobParameters);

            log.info("Label detail job2 started with execution id: {}", jobExecution.getId());

            return ResponseEntity.ok(Map.of(
                    "status", "started",
                    "executionId", jobExecution.getId(),
                    "message", "Label detail job2 has been started successfully"
            ));

        } catch (Exception e) {
            log.error("Failed to start label detail2 job", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "status", "error",
                    "message", "Failed to start label detail job: " + e.getMessage()
            ));
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