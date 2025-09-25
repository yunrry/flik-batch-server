package yunrry.flik.batch.job.reader;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import yunrry.flik.batch.domain.TourismRawData;
import yunrry.flik.batch.exception.RateLimitExceededException;
import yunrry.flik.batch.service.ApiService;
import yunrry.flik.batch.service.RateLimitService;
import yunrry.flik.batch.service.RestaurantApiService;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
@RequiredArgsConstructor
public class RestaurantApiItemReader implements ItemReader<TourismRawData> {

    private final RestaurantApiService apiService;
    private final RateLimitService rateLimitService;

    private List<TourismRawData> currentBatch;
    private AtomicInteger currentIndex = new AtomicInteger(0);
    private AtomicInteger currentPage = new AtomicInteger(1);
    private StepExecution stepExecution;

    private String currentAreaCode = "26"; // 기본 제주

    @Value("${tourism-api.service-key}")
    private String serviceKey;      // 외부에서 주입받을 key

    public void setAreaCode(String areaCode) {
        if (!areaCode.equals(this.currentAreaCode)) {
            this.currentAreaCode = areaCode;
            this.currentPage.set(1);
            this.currentBatch = null;
            this.currentIndex.set(0);
            log.info("Area code changed to {}, resetting pagination", areaCode);
        }
    }

    public void setServiceKey(String serviceKey) {
        this.serviceKey = serviceKey;
        log.info("Service key set externally");
    }

    @BeforeStep
    public void beforeStep(StepExecution stepExecution) {
        this.stepExecution = stepExecution;
        ExecutionContext context = stepExecution.getJobExecution().getExecutionContext();
        String key = "lastPage_" + currentAreaCode;
        int lastPage = context.getInt(key, 0);
        currentPage.set(lastPage + 1);
        log.info("Resuming from page {} for area {}", currentPage.get(), currentAreaCode);
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
            currentBatch = apiService.fetchAreaBasedList(currentPage.get(), currentAreaCode, serviceKey);
            currentIndex.set(0);

            if (currentBatch != null && !currentBatch.isEmpty()) {
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
            ExecutionContext context = stepExecution.getJobExecution().getExecutionContext();
            String key = "lastPage_" + currentAreaCode;
            context.putInt(key, currentPage.get());
            log.debug("Saved page state: {} for area {}", currentPage.get(), currentAreaCode);
        }
    }
}
