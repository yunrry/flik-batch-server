package yunrry.flik.batch.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/api/collect")
@RequiredArgsConstructor
public class TourismDataCollectionController {
    private final JobLauncher jobLauncher;
    private final Job tourismDataCollectionJob;

    @PostMapping("/tourism")
    public ResponseEntity<String> executeTourismBatch(
            @RequestParam String serviceKey,
            @RequestParam String areaCode,
            @RequestParam String contentTypeId,
            @RequestParam(defaultValue = "100") String collectCount) {
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("serviceKey", serviceKey)
                    .addString("areaCode", areaCode)
                    .addString("contentTypeId", contentTypeId)
                    .addString("collectCount", collectCount)
                    .addLong("time", System.currentTimeMillis())
                    .toJobParameters();

            var execution = jobLauncher.run(tourismDataCollectionJob, jobParameters);

            if (execution.getStatus().isUnsuccessful()) {
                return ResponseEntity.status(500)
                        .body("Job failed: " + execution.getExitStatus().getExitDescription());
            }
            return ResponseEntity.ok("Job executed successfully");
        } catch (Exception e) {
            log.error("Job execution failed", e);
            return ResponseEntity.status(500).body("Job failed: " + e.getMessage());
        }
    }
}
