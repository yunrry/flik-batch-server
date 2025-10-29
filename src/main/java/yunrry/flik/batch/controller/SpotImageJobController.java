package yunrry.flik.batch.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;

import java.util.Map;

@RestController
@RequestMapping("/batch")
@RequiredArgsConstructor
public class SpotImageJobController {

    private final JobLauncher jobLauncher;

    @Qualifier("spotImageBackfillJob")
    private final Job spotImageBackfillJob;

    // 실행: POST /batch/spots/images
    @PostMapping("/spots/images")
    public ResponseEntity<?> runSpotImageBackfill() {
        try {
            JobParameters params = new JobParametersBuilder()
                    .addLong("requestTime", System.currentTimeMillis()) // 재실행용 고유 파라미터
                    .toJobParameters();

            JobExecution execution = jobLauncher.run(spotImageBackfillJob, params);

            return ResponseEntity.ok(Map.of(
                    "jobName", execution.getJobInstance().getJobName(),
                    "instanceId", execution.getJobInstance().getId(),
                    "executionId", execution.getId(),
                    "status", execution.getStatus().toString()
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", e.getClass().getSimpleName(),
                    "message", e.getMessage()
            ));
        }
    }
}