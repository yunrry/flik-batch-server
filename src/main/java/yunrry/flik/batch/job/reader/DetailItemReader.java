package yunrry.flik.batch.job.reader;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.stereotype.Component;
import yunrry.flik.batch.domain.TourismRawData;
import yunrry.flik.batch.repository.TourismDataRepository;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class DetailItemReader implements ItemReader<TourismRawData> {

    private final TourismDataRepository repository;
    private List<TourismRawData> unprocessedData;
    private int currentIndex = 0;
    private boolean isInitialized = false;

    @Override
    public TourismRawData read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {

        // 첫 번째 호출 시 데이터 로드
        if (!isInitialized) {
            initializeData();
        }

        // 데이터가 없거나 모든 데이터를 읽었으면 null 반환 (읽기 종료)
        if (unprocessedData == null || unprocessedData.isEmpty() || currentIndex >= unprocessedData.size()) {
            log.info("DetailItemReader completed. Total processed: {}", currentIndex);
            return null;
        }

        TourismRawData data = unprocessedData.get(currentIndex);
        currentIndex++;

        if (currentIndex % 100 == 0) {
            log.info("DetailItemReader progress: {}/{}", currentIndex, unprocessedData.size());
        }

        return data;
    }

    private void initializeData() {
        try {
            log.info("DetailItemReader initializing - Loading unprocessed data for detail enrichment");
            unprocessedData = repository.findUnprocessedForDetail();

            if (unprocessedData == null) {
                log.warn("Repository returned null for unprocessed data");
                unprocessedData = List.of(); // 빈 리스트로 초기화
            }

            log.info("DetailItemReader loaded {} items for detail processing", unprocessedData.size());

            if (unprocessedData.isEmpty()) {
                log.warn("No unprocessed data found for detail enrichment. All items may already have detail information.");
            }

            currentIndex = 0;
            isInitialized = true;

        } catch (Exception e) {
            log.error("Failed to initialize DetailItemReader", e);
            unprocessedData = List.of(); // 오류 시 빈 리스트로 안전하게 처리
            isInitialized = true;
        }
    }

    /**
     * Reader 상태 초기화 (재실행 시 호출)
     */
    public void reset() {
        log.info("DetailItemReader reset called");
        unprocessedData = null;
        currentIndex = 0;
        isInitialized = false;
    }

    /**
     * 현재 진행 상태 조회
     */
    public String getProgress() {
        if (!isInitialized || unprocessedData == null) {
            return "Not initialized";
        }
        return String.format("%d/%d (%.1f%%)",
                currentIndex,
                unprocessedData.size(),
                unprocessedData.size() > 0 ? (currentIndex * 100.0 / unprocessedData.size()) : 0);
    }
}