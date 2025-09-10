package yunrry.flik.batch.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import yunrry.flik.batch.job.GooglePlacesEnrichmentJob;
import yunrry.flik.batch.service.NotificationService;
import yunrry.flik.batch.service.RateLimitService;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/batch")
@RequiredArgsConstructor
public class BatchGoogleController {

    private final JobLauncher jobLauncher;
    private final Job tourismDataJob;
    private final Job googlePlacesJob; // 추가
    private final JobRepository jobRepository;
    private final RateLimitService rateLimitService;
    private final GooglePlacesEnrichmentJob googlePlacesEnrichmentJob; // 추가
    private final JdbcTemplate jdbcTemplate; // 추가
    private final NotificationService notificationService;


    @PostMapping("/google-places/run")
    public ResponseEntity<Map<String, Object>> runGooglePlacesBatch() {
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
                    .addString("triggerType", "MANUAL_GOOGLE_PLACES")
                    .toJobParameters();

            JobExecution jobExecution = jobLauncher.run(googlePlacesJob, jobParameters);

            response.put("success", true);
            response.put("jobExecutionId", jobExecution.getId());
            response.put("status", jobExecution.getStatus().toString());
            response.put("startTime", jobExecution.getStartTime());
            response.put("apiCallsRemaining", rateLimitService.getRemainingCount());

            log.info("Google Places enrichment job started manually - Execution ID: {}", jobExecution.getId());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Failed to start Google Places enrichment job", e);
            response.put("success", false);
            response.put("message", "Failed to start Google Places job: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PostMapping("/google-places/run/{tableType}")
    public ResponseEntity<Map<String, Object>> runGooglePlacesBatchByType(@PathVariable String tableType) {
        Map<String, Object> response = new HashMap<>();

        try {
            // API 제한 확인
            if (!rateLimitService.canMakeRequest()) {
                response.put("success", false);
                response.put("message", "API rate limit exceeded");
                return ResponseEntity.badRequest().body(response);
            }

            // 테이블 타입별 Step 실행
            Step targetStep = switch (tableType.toLowerCase()) {
                case "attractions" -> googlePlacesEnrichmentJob.enrichTouristAttractionsStep();
                case "restaurants" -> googlePlacesEnrichmentJob.enrichRestaurantsStep();
                case "accommodations" -> googlePlacesEnrichmentJob.enrichAccommodationsStep();
                case "cultural" -> googlePlacesEnrichmentJob.enrichCulturalFacilitiesStep();
                case "leisure" -> googlePlacesEnrichmentJob.enrichLeisureSportsStep();
                case "shopping" -> googlePlacesEnrichmentJob.enrichShoppingStep();
                default -> throw new IllegalArgumentException("Invalid table type: " + tableType);
            };

            // 단일 Step Job 생성 및 실행 (리스너 추가)
            Job singleStepJob = new JobBuilder("googlePlaces_" + tableType + "_job", jobRepository)
                    .listener(createSingleTableJobListener(tableType))
                    .start(targetStep)
                    .build();

            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("executionTime", LocalDateTime.now().toString())
                    .addString("triggerType", "MANUAL_SINGLE_TABLE")
                    .addString("tableType", tableType)
                    .toJobParameters();

            JobExecution jobExecution = jobLauncher.run(singleStepJob, jobParameters);

            // 시작 알림 전송
            notificationService.sendGooglePlacesSingleTableStartAlert(jobExecution.getId(), tableType);

            response.put("success", true);
            response.put("jobExecutionId", jobExecution.getId());
            response.put("status", jobExecution.getStatus().toString());
            response.put("tableType", tableType);
            response.put("startTime", jobExecution.getStartTime());

            log.info("Google Places {} enrichment started - Execution ID: {}", tableType, jobExecution.getId());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Failed to start Google Places {} enrichment", tableType, e);
            response.put("success", false);
            response.put("message", "Failed to start job: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/google-places/status/{jobExecutionId}")
    public ResponseEntity<Map<String, Object>> getGooglePlacesStatus(@PathVariable Long jobExecutionId) {
        Map<String, Object> response = new HashMap<>();

        try {
            JobExecution jobExecution = jobRepository.getLastJobExecution("googlePlacesJob",
                    new JobParametersBuilder().toJobParameters());

            if (jobExecution == null || !jobExecution.getId().equals(jobExecutionId)) {
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

            // Google Places 관련 Step 정보
            response.put("steps", jobExecution.getStepExecutions().stream()
                    .map(this::mapGooglePlacesStepExecution)
                    .toList());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Failed to get Google Places batch status", e);
            response.put("success", false);
            response.put("message", "Failed to get status: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/google-places/summary")
    public ResponseEntity<Map<String, Object>> getGooglePlacesSummary() {
        Map<String, Object> response = new HashMap<>();

        try {
            Map<String, Object> attractions = getTableSummary("fetched_tourist_attractions");
            Map<String, Object> restaurants = getTableSummary("fetched_restaurants");
            Map<String, Object> accommodations = getTableSummary("fetched_accommodations");
            Map<String, Object> cultural = getTableSummary("fetched_cultural_facilities");
            Map<String, Object> leisure = getTableSummary("fetched_sports_recreation");
            Map<String, Object> shopping = getTableSummary("fetched_shopping");

            response.put("success", true);
            response.put("attractions", attractions);
            response.put("restaurants", restaurants);
            response.put("accommodations", accommodations);
            response.put("cultural", cultural);
            response.put("leisure", leisure);
            response.put("shopping", shopping);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Failed to get Google Places summary", e);
            response.put("success", false);
            response.put("message", "Failed to get summary: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    private Map<String, Object> getTableSummary(String tableName) {
        Map<String, Object> summary = new HashMap<>();

        String totalCountSql = "SELECT COUNT(*) FROM " + tableName;
        String enrichedCountSql = "SELECT COUNT(*) FROM " + tableName + " WHERE google_rating IS NOT NULL";
        String avgRatingSql = "SELECT AVG(google_rating) FROM " + tableName + " WHERE google_rating IS NOT NULL";

        Integer totalCount = jdbcTemplate.queryForObject(totalCountSql, Integer.class);
        Integer enrichedCount = jdbcTemplate.queryForObject(enrichedCountSql, Integer.class);
        Double avgRating = jdbcTemplate.queryForObject(avgRatingSql, Double.class);

        summary.put("totalCount", totalCount != null ? totalCount : 0);
        summary.put("enrichedCount", enrichedCount != null ? enrichedCount : 0);
        summary.put("pendingCount", (totalCount != null ? totalCount : 0) - (enrichedCount != null ? enrichedCount : 0));
        summary.put("averageRating", avgRating != null ? Math.round(avgRating * 100.0) / 100.0 : 0.0);
        summary.put("enrichmentProgress", totalCount != null && totalCount > 0 ?
                Math.round(((double) (enrichedCount != null ? enrichedCount : 0) / totalCount) * 100.0) : 0.0);

        return summary;
    }

    private Map<String, Object> mapGooglePlacesStepExecution(StepExecution stepExecution) {
        Map<String, Object> stepInfo = new HashMap<>();
        stepInfo.put("stepName", stepExecution.getStepName());
        stepInfo.put("status", stepExecution.getStatus().toString());
        stepInfo.put("readCount", stepExecution.getReadCount());
        stepInfo.put("writeCount", stepExecution.getWriteCount());
        stepInfo.put("skipCount", stepExecution.getSkipCount());
        stepInfo.put("startTime", stepExecution.getStartTime());
        stepInfo.put("endTime", stepExecution.getEndTime());
        stepInfo.put("exitStatus", stepExecution.getExitStatus().getExitCode());

        // Google Places 전용 정보 추가
        if (stepExecution.getStepName().contains("enrich")) {
            stepInfo.put("tableType", extractTableType(stepExecution.getStepName()));
            stepInfo.put("successRate", stepExecution.getWriteCount() > 0 ?
                    Math.round(((double) stepExecution.getWriteCount() / stepExecution.getReadCount()) * 100.0) : 0.0);
        }

        return stepInfo;
    }

    private String extractTableType(String stepName) {
        if (stepName.contains("TouristAttractions")) return "attractions";
        if (stepName.contains("Restaurants")) return "restaurants";
        if (stepName.contains("Accommodations")) return "accommodations";
        if (stepName.contains("CulturalFacilities")) return "cultural";
        if (stepName.contains("LeisureSports")) return "leisure";
        if (stepName.contains("Shopping")) return "shopping";
        return "unknown";
    }

    private JobExecutionListener createSingleTableJobListener(String tableType) {
        return new JobExecutionListener() {
            @Override
            public void beforeJob(JobExecution jobExecution) {
                // 시작 알림은 컨트롤러에서 처리
            }

            @Override
            public void afterJob(JobExecution jobExecution) {
                notificationService.sendGooglePlacesSingleTableCompletionAlert(jobExecution, tableType);
            }
        };
    }
}
