package yunrry.flik.batch.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class BatchScheduler {

    private final JobLauncher jobLauncher;
    private final Job tourismDataJob;

    @Scheduled(cron = "0 0 2 * * ?") // 매일 새벽 2시
    public void executeTourismDataBatch() {
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("executionTime", LocalDateTime.now().toString())
                    .toJobParameters();

            jobLauncher.run(tourismDataJob, jobParameters);
            log.info("Tourism data batch job completed successfully");
        } catch (Exception e) {
            log.error("Failed to execute tourism data batch job", e);
        }
    }
}