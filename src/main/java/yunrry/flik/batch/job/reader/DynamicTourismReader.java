package yunrry.flik.batch.job.reader;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.ItemReader;
import org.springframework.stereotype.Component;
import yunrry.flik.batch.domain.ApiCallHistory;
import yunrry.flik.batch.domain.TourismRawData;
import yunrry.flik.batch.exception.ApiLimitExceededException;
import yunrry.flik.batch.exception.RateLimitExceededException;
import yunrry.flik.batch.repository.TourismDataRepository;
import yunrry.flik.batch.service.ApiService;
import yunrry.flik.batch.service.RateLimitService;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
@RequiredArgsConstructor
public class DynamicTourismReader implements ItemReader<TourismRawData> {

    private final ApiService apiService;
    private final RateLimitService rateLimitService;
    private final TourismDataRepository tourismDataRepository;

    private List<TourismRawData> currentBatch;
    private AtomicInteger currentIndex = new AtomicInteger(0);
    private AtomicInteger currentPage = new AtomicInteger(1);

    private String serviceKey;
    private String areaCode;
    private String contentTypeId;
    private String collectCount;

    @BeforeStep
    public void beforeStep(StepExecution stepExecution) {
        JobParameters jobParameters = stepExecution.getJobParameters();
        this.serviceKey = jobParameters.getString("serviceKey");
        this.areaCode = jobParameters.getString("areaCode");
        this.contentTypeId = jobParameters.getString("contentTypeId");
        this.collectCount = jobParameters.getString("collectCount", "100");

        // 이전 호출 이력 조회하여 시작 페이지 설정
        ApiCallHistory history = tourismDataRepository.getLastApiCallHistory(contentTypeId, areaCode);

        if (history != null) {
            currentPage.set(history.getLastPageNo() + 1);
        } else {
            currentPage.set(1);  // 기록이 없는 경우 1페이지부터 시작
        }

        log.info("DynamicTourismReader initialized - area: {}, type: {}, startPage: {}",
                areaCode, contentTypeId, currentPage.get());
    }

    @Override
    public TourismRawData read() throws Exception {
        try {
            rateLimitService.checkRateLimit();
        } catch (RateLimitExceededException e) {
            log.warn("Rate limit exceeded: {}", e.getMessage());
            return null;
        }

        log.debug("Reading data - currentBatch: {}, currentIndex: {}",
                currentBatch != null ? currentBatch.size() : "null", currentIndex.get());

        if (currentBatch == null || currentIndex.get() >= currentBatch.size()) {
            log.info("Fetching next batch - page: {}", currentPage.get());
            fetchNextBatch();
        }

        if (currentBatch == null || currentBatch.isEmpty()) {
            log.info("No more data available, ending read");
            return null;
        }

        return currentBatch.get(currentIndex.getAndIncrement());
    }

    private void fetchNextBatch() {
        try {
            currentBatch = apiService.fetchAreaBasedListByContentType(
                    currentPage.get(), areaCode, contentTypeId,
                    Integer.parseInt(collectCount), serviceKey
            );

            currentIndex.set(0);

            if (currentBatch != null && !currentBatch.isEmpty()) {
                // 성공시에만 이력 저장
                tourismDataRepository.saveApiCallHistory(contentTypeId, areaCode,
                        currentPage.get(), Integer.parseInt(collectCount));
                currentPage.incrementAndGet();
            }
        } catch (ApiLimitExceededException e) {
            log.error("API limit exceeded at page: {}", currentPage.get());
            // 이력 업데이트 없이 중단
            currentBatch = null;
            throw e;
        }
    }
}