package yunrry.flik.batch.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class BatchScheduler {

    private final JobLauncher jobLauncher;
    private final Job tourismDataJob;
    private final Job allMigrationJob;
    private final Job tourismDataCollectionJob;

    @Value("${tourism-api.service-key}")
    private String serviceKey;

    @Scheduled(cron = "0 0 1,19 * * ?", zone = "Asia/Seoul") // 매일 새벽 1시
    public void executeTourismDataCollectionBatch() {
        String[] areaCodes = {"2", "3", "4", "5", "6", "7", "8", "31", "32", "33", "34", "35", "36", "37", "38", "39", "1"};
        String[] contentTypeIds = {"12", "14", "15", "28", "32", "38", "39"};

        for (String areaCode : areaCodes) {
            for (String contentTypeId : contentTypeIds) {
                try {
                    JobParameters jobParameters = new JobParametersBuilder()
                            .addString("serviceKey", serviceKey)  // @Value로 주입받은 값 사용
                            .addString("areaCode", areaCode)
                            .addString("contentTypeId", contentTypeId)
                            .addString("collectCount", "100")
                            .addString("executionTime", LocalDateTime.now().toString())
                            .toJobParameters();

                    jobLauncher.run(tourismDataCollectionJob, jobParameters);
                    Thread.sleep(1000);
                } catch (Exception e) {
                    log.error("Failed - area: {}, type: {}", areaCode, contentTypeId, e);
                }
            }
        }
    }


//
//    @Scheduled(cron = "0 0 2 * * ?") // 매일 새벽 2시
//    public void executeTourismDataBatch() {
//        try {
//            JobParameters jobParameters = new JobParametersBuilder()
//                    .addString("executionTime", LocalDateTime.now().toString())
//                    .toJobParameters();
//
//            jobLauncher.run(tourismDataJob, jobParameters);
//            log.info("Tourism data batch job completed successfully");
//        } catch (Exception e) {
//            log.error("Failed to execute tourism data batch job", e);
//        }
//    }

    @Scheduled(cron = "0 0 4,20 * * ?", zone = "Asia/Seoul") // 매일 새벽 4시
    public void executeAllMigrationBatch() {
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("executionTime", LocalDateTime.now().toString())
                    .toJobParameters();

            jobLauncher.run(allMigrationJob, jobParameters);
            log.info("All migration batch job completed successfully");
        } catch (Exception e) {
            log.error("Failed to execute all migration batch job", e);
        }
    }
}