package yunrry.flik.batch.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.StepExecution;
import org.springframework.stereotype.Component;
import yunrry.flik.batch.service.NotificationService;
import yunrry.flik.batch.service.RateLimitService;

@Slf4j
@Component
@RequiredArgsConstructor
public class BatchJobListener implements JobExecutionListener {

    private final NotificationService notificationService;
    private final RateLimitService rateLimitService;

    @Override
    public void afterJob(JobExecution jobExecution) {
        String jobName = jobExecution.getJobInstance().getJobName();
        String status = jobExecution.getStatus().toString();

        long totalRead = jobExecution.getStepExecutions().stream()
                .mapToLong(StepExecution::getReadCount).sum();
        long totalWrite = jobExecution.getStepExecutions().stream()
                .mapToLong(StepExecution::getWriteCount).sum();
        long totalSkip = jobExecution.getStepExecutions().stream()
                .mapToLong(StepExecution::getSkipCount).sum();


        log.info("Job {} completed - Read: {}, Written: {}, Skipped: {}, API calls: {}",
                jobName, totalRead, totalWrite, totalSkip, rateLimitService.getCurrentCount());

        if (status.equals("COMPLETED") && rateLimitService.getCurrentCount() >= 1000) {
            notificationService.sendRateLimitAlert(
                    jobExecution.getJobInstance().getJobName(),
                    rateLimitService.getCurrentCount()
            );
        }

        // 수집 건수가 0이면 알림 발송
        if (totalWrite == 0 && status.equals("COMPLETED")) {
            notificationService.sendEmptyDataAlert(jobName);
        } else {
            notificationService.sendBatchCompletionAlert(jobName, totalWrite, status);
        }
    }
}
