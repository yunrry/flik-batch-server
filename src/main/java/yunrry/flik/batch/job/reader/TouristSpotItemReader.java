package yunrry.flik.batch.job.reader;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
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
public class TouristSpotItemReader implements ItemReader<TourismRawData> {

    private final ApiService apiService;
    private final RateLimitService rateLimitService;

    private List<TourismRawData> currentBatch;
    private AtomicInteger currentIndex = new AtomicInteger(0);
    private AtomicInteger currentPage = new AtomicInteger(1);

    private String serviceKey;
    private String areaCode;
    private String collectCount;
    private static final String CONTENT_TYPE_ID = "12"; // 관광지

    @BeforeStep
    public void beforeStep(StepExecution stepExecution) {
        JobParameters jobParameters = stepExecution.getJobParameters();
        this.serviceKey = jobParameters.getString("serviceKey");
        this.areaCode = jobParameters.getString("areaCode");
        this.collectCount = jobParameters.getString("collectCount", "100");

        log.info("TouristSpotItemReader initialized - areaCode: {}, contentType: 관광지", areaCode);
    }

    @Override
    public TourismRawData read() throws Exception {
        try {
            rateLimitService.checkRateLimit();
        } catch (RateLimitExceededException e) {
            log.warn("Rate limit exceeded, stopping batch: {}", e.getMessage());
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
            currentBatch = apiService.fetchAreaBasedListByContentType(
                    currentPage.get(),
                    areaCode,
                    CONTENT_TYPE_ID,
                    Integer.parseInt(collectCount),
                    serviceKey
            );

            currentIndex.set(0);

            if (currentBatch != null && !currentBatch.isEmpty()) {
                currentPage.incrementAndGet();
                log.info("Fetched {} tourist spots from page {}", currentBatch.size(), currentPage.get() - 1);
            }
        } catch (Exception e) {
            log.error("Failed to fetch tourist spot data from API at page: {}", currentPage.get(), e);
            currentBatch = null;
        }
    }
}