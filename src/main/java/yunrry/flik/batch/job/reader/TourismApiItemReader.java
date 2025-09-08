package yunrry.flik.batch.job.reader;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemReader;
import org.springframework.stereotype.Component;
import yunrry.flik.batch.domain.TourismRawData;
import yunrry.flik.batch.exception.RateLimitExceededException;
import yunrry.flik.batch.service.ApiService;
import yunrry.flik.batch.service.RateLimitService;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
@RequiredArgsConstructor
public class TourismApiItemReader implements ItemReader<TourismRawData> {

    private final ApiService apiService;
    private final RateLimitService rateLimitService;

    private List<TourismRawData> currentBatch;
    private AtomicInteger currentIndex = new AtomicInteger(0);
    private AtomicInteger currentPage = new AtomicInteger(1);
    private boolean isDetailMode = false;
    private StepExecution stepExecution;


    @BeforeStep
    public void beforeStep(StepExecution stepExecution) {
        this.stepExecution = stepExecution;

        // 이전 실행에서 중단된 페이지 복구
        JobExecution jobExecution = stepExecution.getJobExecution();
        ExecutionContext context = jobExecution.getExecutionContext();

        if (context.containsKey("lastPage")) {
            int lastPage = context.getInt("lastPage");
            currentPage.set(lastPage + 1);
            log.info("Resuming from page: {}", currentPage.get());
        }
    }

    public TourismRawData read() throws Exception {
        try {
            rateLimitService.checkRateLimit();
        } catch (RateLimitExceededException e) {
            log.warn("Rate limit exceeded, stopping batch: {}", e.getMessage());
            // 배치 정상 종료 처리
            return null;
        }

        if (currentBatch == null || currentIndex.get() >= currentBatch.size()) {
            fetchNextBatch();
        }

        if (currentBatch == null || currentBatch.isEmpty()) {
            return null;
        }

        return currentBatch.get(currentIndex.getAndIncrement());
    }

    private void fetchNextBatch() {
        try {
            currentBatch = apiService.fetchAreaBasedList(currentPage.get());
            currentIndex.set(0);

            if (currentBatch != null && !currentBatch.isEmpty()) {
                // 성공적으로 페이지 처리 시 상태 저장
                savePageState();
                currentPage.incrementAndGet();
            }
        } catch (Exception e) {
            log.error("Failed to fetch data from API at page: {}", currentPage.get(), e);
            currentBatch = null;
        }
    }

    private void savePageState() {
        if (stepExecution != null) {
            JobExecution jobExecution = stepExecution.getJobExecution();
            ExecutionContext context = jobExecution.getExecutionContext();
            context.putInt("lastPage", currentPage.get());
            log.debug("Saved page state: {}", currentPage.get());
        }
    }
}