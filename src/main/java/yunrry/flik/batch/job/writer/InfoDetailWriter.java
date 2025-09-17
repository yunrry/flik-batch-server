package yunrry.flik.batch.job.writer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;
import yunrry.flik.batch.domain.TourismRawData;
import yunrry.flik.batch.repository.TourismDataRepository;

@RequiredArgsConstructor
@Component("infoDetailWriter")
@Slf4j
public class InfoDetailWriter implements ItemWriter<TourismRawData> {

    private final TourismDataRepository tourismDataRepository;

    @Override
    public void write(Chunk<? extends TourismRawData> chunk) throws Exception {
        for (TourismRawData data : chunk) {
            try {
                tourismDataRepository.updateDetailData(data);
                log.debug("Updated info detail for contentId: {}", data.getContentId());
            } catch (Exception e) {
                log.error("Failed to update info detail for contentId: {}",
                        data.getContentId(), e);
            }
        }
    }
}