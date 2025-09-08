package yunrry.flik.batch.job.writer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;
import yunrry.flik.batch.domain.TourismRawData;
import yunrry.flik.batch.repository.TourismDataRepository;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class TourismDataWriter implements ItemWriter<TourismRawData> {

    private final TourismDataRepository tourismDataRepository;

    @Override
    public void write(Chunk<? extends TourismRawData> chunk) throws Exception {
        int successCount = 0;
        int failCount = 0;

        for (TourismRawData item : chunk) {
            try {
                tourismDataRepository.saveAreaBasedData(item);
                successCount++;
                log.debug("Saved item: contentId={}, title={}", item.getContentId(), item.getTitle());
            } catch (Exception e) {
                failCount++;
                log.error("Failed to save item: contentId={}, error={}", item.getContentId(), e.getMessage());
                throw e;
            }
        }

        log.info("Batch write completed - Success: {}, Failed: {}, Total: {}",
                successCount, failCount, chunk.size());
    }

    public ItemWriter<TourismRawData> createDetailWriter() {
        return new ItemWriter<TourismRawData>() {
            @Override
            public void write(Chunk<? extends TourismRawData> chunk) throws Exception {
                try {
                    for (TourismRawData item : chunk) {
                        tourismDataRepository.updateDetailData(item);
                    }
                    log.info("Updated {} tourism detail data items", chunk.size());
                } catch (Exception e) {
                    log.error("Error updating tourism detail data", e);
                    throw e;
                }
            }
        };
    }
}