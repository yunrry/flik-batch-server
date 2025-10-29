package yunrry.flik.batch.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class BatchScheduler {

    private static final String[] AREA_CODES = {
            "2", "3", "4", "5", "6", "7", "8", "31", "32", "33", "34", "35", "36", "37", "38", "39", "1"
    };
    // 관광타입(12:관광지, 14:문화시설, 15:축제공연행사, 28:레포츠, 32:숙박, 38:쇼핑, 39:음식점)
    private static final String[] CONTENT_TYPE_IDS = { "12", "14", "15", "28", "32", "38", "39" };
    private static final String DEFAULT_COLLECT_COUNT = "100";
    private static final long BETWEEN_RUN_DELAY_MS = 1000L;

    private final JobLauncher jobLauncher;
    private final JobExplorer jobExplorer;

    private final Job tourismDataJob;
    private final Job allMigrationJob;
    private final Job tourismDataCollectionJob;
    private final Job spotImageBackfillJob;

    @Value("${tourism-api.service-key}")
    private String serviceKey;

    // 매 분기(1,4,7,10월) 1일 01시, 19시 실행
    @Scheduled(cron = "0 0 1 1 1,4,7,10 ?", zone = "Asia/Seoul")
    public void executeTourismDataCollectionBatch() {
        // 겹침 방지: 이전 실행이 남아있으면 건너뜀
        if (!jobExplorer.findRunningJobExecutions(tourismDataCollectionJob.getName()).isEmpty()) {
            log.warn("Skip scheduling. Job '{}' is still running.", tourismDataCollectionJob.getName());
            return;
        }

        for (String areaCode : AREA_CODES) {
            for (String contentTypeId : CONTENT_TYPE_IDS) {
                try {
                    JobParameters jobParameters = new JobParametersBuilder()
                            .addString("serviceKey", serviceKey)
                            .addString("areaCode", areaCode)
                            .addString("contentTypeId", contentTypeId)
                            .addString("collectCount", DEFAULT_COLLECT_COUNT)
                            .addString("executionTime", LocalDateTime.now().toString())
                            // 유일성 보장 파라미터
                            .addLong("run.id", System.currentTimeMillis())
                            .toJobParameters();

                    log.info("Launch '{}' areaCode={}, contentTypeId={}, collectCount={}",
                            tourismDataCollectionJob.getName(), areaCode, contentTypeId, DEFAULT_COLLECT_COUNT);

                    jobLauncher.run(tourismDataCollectionJob, jobParameters);

                    TimeUnit.MILLISECONDS.sleep(BETWEEN_RUN_DELAY_MS);
                } catch (Exception e) {
                    log.error("Failed - area: {}, type: {}", areaCode, contentTypeId, e);
                }
            }
        }
    }


    @Scheduled(cron = "0 0 4 1 1,4,7,10 ?", zone = "Asia/Seoul")
    public void executeAllMigrationBatch() {
        if (!jobExplorer.findRunningJobExecutions(allMigrationJob.getName()).isEmpty()) {
            log.warn("Skip scheduling. Job '{}' is still running.", allMigrationJob.getName());
            return;
        }

        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("executionTime", LocalDateTime.now().toString())
                    .addLong("run.id", System.currentTimeMillis())
                    .toJobParameters();

            log.info("Launch '{}'", allMigrationJob.getName());
            jobLauncher.run(allMigrationJob, jobParameters);
            log.info("All migration batch job completed successfully");
        } catch (Exception e) {
            log.error("Failed to execute all migration batch job", e);
        }
    }

    @Scheduled(cron = "0 0 5 1 1,4,7,10 ?", zone = "Asia/Seoul")
    public void executeSpotImageBackfillBatch() {
        if (!jobExplorer.findRunningJobExecutions(spotImageBackfillJob.getName()).isEmpty()) {
            log.warn("Skip scheduling. Job '{}' is still running.", spotImageBackfillJob.getName());
            return;
        }

        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("executionTime", LocalDateTime.now().toString())
                    .addLong("run.id", System.currentTimeMillis())
                    .toJobParameters();

            log.info("Launch '{}'", spotImageBackfillJob.getName());
            jobLauncher.run(spotImageBackfillJob, jobParameters);
            log.info("Spot image backfill batch job completed successfully");
        } catch (Exception e) {
            log.error("Failed to execute spot image backfill batch job", e);
        }
    }
}