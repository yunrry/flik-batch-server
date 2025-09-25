// BatchController.java
package yunrry.flik.batch.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import yunrry.flik.batch.job.processor.LabelDetailProcessor;
import yunrry.flik.batch.job.processor.RestaurantDataProcessor;
import yunrry.flik.batch.job.reader.DetailItemReader;
import yunrry.flik.batch.job.reader.RestaurantApiItemReader;
import yunrry.flik.batch.service.RateLimitService;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/category")
@RequiredArgsConstructor
public class CategoryBatchController {

    private final JobLauncher jobLauncher;
    private final Job restaurantDataJob;
    private final RateLimitService rateLimitService;
    private final RestaurantApiItemReader restaurantApiItemReader;
    private final RestaurantDataProcessor restaurantDataProcessor;
    private final LabelDetailProcessor labelDetailProcessor;

    @GetMapping("/test")
    public String test() {
        return "Controller working";
    }

    @PostMapping("/restaurant/run")
    public ResponseEntity<Map<String, Object>> runRestaurantBatch(
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
            restaurantApiItemReader.setAreaCode(areaCode);
            restaurantApiItemReader.setServiceKey(serviceKey);
            restaurantDataProcessor.setServiceKey(serviceKey);
            labelDetailProcessor.setServiceKey(serviceKey);
            log.info("start restaurant batch job for areaCode: {}, serviceKey: {}", areaCode, serviceKey);

            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("executionTime", LocalDateTime.now().toString())
                    .addString("triggerType", "MANUAL")
                    .toJobParameters();

            JobExecution jobExecution = jobLauncher.run(restaurantDataJob, jobParameters);

            response.put("success", true);
            response.put("jobExecutionId", jobExecution.getId());
            response.put("status", jobExecution.getStatus().toString());
            response.put("startTime", jobExecution.getStartTime());
            response.put("apiCallsRemaining", rateLimitService.getRemainingCount());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Failed to start Restaurant batch job", e);
            response.put("success", false);
            response.put("message", "Failed to start batch job: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }


}