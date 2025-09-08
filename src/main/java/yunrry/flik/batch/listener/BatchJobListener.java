package yunrry.flik.batch.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.StepExecution;
import org.springframework.stereotype.Component;
import yunrry.flik.batch.service.NotificationService;

@Slf4j
@Component
@RequiredArgsConstructor
public class BatchJobListener implements JobExecutionListener {

    private final NotificationService notificationService;

    @Override
    public void afterJob(JobExecution jobExecution) {
        String jobName = jobExecution.getJobInstance().getJobName();
        String status = jobExecution.getStatus().toString();

        long totalWriteCount = jobExecution.getStepExecutions().stream()
                .mapToLong(StepExecution::getWriteCount)
                .sum();

        log.info("Job {} completed with status: {}, total records: {}",
                jobName, status, totalWriteCount);

        // 수집 건수가 0이면 알림 발송
        if (totalWriteCount == 0 && status.equals("COMPLETED")) {
            notificationService.sendEmptyDataAlert(jobName);
        } else {
            notificationService.sendBatchCompletionAlert(jobName, totalWriteCount, status);
        }
    }
}
