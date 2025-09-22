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
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/migration")
@RequiredArgsConstructor
public class MigrationController {

    private final JobLauncher jobLauncher;
    private final Job allMigrationJob;
    private final Job accommodationMigrationJob;
    private final Job culturalMigrationJob;
    private final Job festivalMigrationJob;
    private final Job restaurantMigrationJob;
    private final Job shoppingMigrationJob;
    private final Job sportsMigrationJob;
    private final Job touristMigrationJob;

    @PostMapping("/all")
    public ResponseEntity<Map<String, Object>> executeAllMigration() {
        return executeJob(allMigrationJob, "allMigrationJob");
    }

    @PostMapping("/accommodation")
    public ResponseEntity<Map<String, Object>> executeAccommodationMigration() {
        return executeJob(accommodationMigrationJob, "accommodationMigrationJob");
    }

    @PostMapping("/cultural")
    public ResponseEntity<Map<String, Object>> executeCulturalMigration() {
        return executeJob(culturalMigrationJob, "culturalMigrationJob");
    }

    @PostMapping("/festival")
    public ResponseEntity<Map<String, Object>> executeFestivalMigration() {
        return executeJob(festivalMigrationJob, "festivalMigrationJob");
    }

    @PostMapping("/restaurant")
    public ResponseEntity<Map<String, Object>> executeRestaurantMigration() {
        return executeJob(restaurantMigrationJob, "restaurantMigrationJob");
    }

    @PostMapping("/shopping")
    public ResponseEntity<Map<String, Object>> executeShoppingMigration() {
        return executeJob(shoppingMigrationJob, "shoppingMigrationJob");
    }

    @PostMapping("/sports")
    public ResponseEntity<Map<String, Object>> executeSportsMigration() {
        return executeJob(sportsMigrationJob, "sportsMigrationJob");
    }

    @PostMapping("/tourist")
    public ResponseEntity<Map<String, Object>> executeTouristMigration() {
        return executeJob(touristMigrationJob, "touristMigrationJob");
    }

    private ResponseEntity<Map<String, Object>> executeJob(Job job, String jobName) {
        Map<String, Object> response = new HashMap<>();

        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("executionTime", LocalDateTime.now().toString())
                    .toJobParameters();

            jobLauncher.run(job, jobParameters);

            response.put("success", true);
            response.put("message", jobName + " executed successfully");
            response.put("executionTime", LocalDateTime.now());

            log.info("{} executed successfully", jobName);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to execute " + jobName);
            response.put("error", e.getMessage());
            response.put("executionTime", LocalDateTime.now());

            log.error("Failed to execute {}", jobName, e);
            return ResponseEntity.internalServerError().body(response);
        }
    }
}