package yunrry.flik.batch.job.reader;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.stereotype.Component;
import yunrry.flik.batch.domain.TourismRawData;
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

    @Override
    public TourismRawData read() throws Exception {

        if (!rateLimitService.canMakeRequest()) {
            log.warn("API rate limit exceeded for today");
            return null;
        }

        if (currentBatch == null || currentIndex.get() >= currentBatch.size()) {
            fetchNextBatch();
        }

        if (currentBatch == null || currentBatch.isEmpty()) {
            return null; // End of data
        }

        return currentBatch.get(currentIndex.getAndIncrement());
    }

    private void fetchNextBatch() {
        try {
            if (isDetailMode) {
                currentBatch = apiService.fetchUnprocessedDataForDetail();
            } else {
                currentBatch = apiService.fetchAreaBasedList(currentPage.get());
                currentPage.incrementAndGet();
            }
            currentIndex.set(0);
        } catch (Exception e) {
            log.error("Failed to fetch data from API", e);
            currentBatch = null;
        }
    }

    public ItemReader<TourismRawData> createDetailReader() {
        TourismApiItemReader detailReader = new TourismApiItemReader(apiService, rateLimitService);
        detailReader.isDetailMode = true;
        return detailReader;
    }
}