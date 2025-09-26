package yunrry.flik.batch.job.writer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;
import yunrry.flik.batch.domain.TourismRawData;
import yunrry.flik.batch.repository.TourismDataRepository;

@Slf4j
@Component
@RequiredArgsConstructor
public class TouristSpotWriter implements ItemWriter<TourismRawData> {

    private final TourismDataRepository tourismDataRepository;

    @Override
    public void write(Chunk<? extends TourismRawData> chunk) throws Exception {
        int successCount = 0;
        int failCount = 0;

        for (TourismRawData item : chunk) {
            try {
                // 통합 저장 (기본정보 + 상세정보 + 라벨정보)
                tourismDataRepository.saveAreaBasedData(item);
                tourismDataRepository.updateDetailData(item);
                tourismDataRepository.updateLabelDetailData(item);

                successCount++;
                log.debug("Saved complete tourist spot: contentId={}, title={}",
                        item.getContentId(), item.getTitle());
            } catch (Exception e) {
                failCount++;
                log.error("Failed to save tourist spot: contentId={}, error={}",
                        item.getContentId(), e.getMessage());
                throw e;
            }
        }

        log.info("Tourist spot write completed - Success: {}, Failed: {}, Total: {}",
                successCount, failCount, chunk.size());
    }
}