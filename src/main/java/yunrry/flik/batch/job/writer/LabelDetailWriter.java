package yunrry.flik.batch.job.writer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;
import yunrry.flik.batch.domain.TourismRawData;
import yunrry.flik.batch.repository.TourismDataRepository;

@RequiredArgsConstructor
@Component
@Slf4j
public class LabelDetailWriter implements ItemWriter<TourismRawData> {

    private final TourismDataRepository tourismDataRepository;

    @Override
    public void write(Chunk<? extends TourismRawData> chunk) throws Exception {
        for (TourismRawData data : chunk) {
            try {
                tourismDataRepository.updateLabelDetailData(data);
                log.debug("Updated label detail for contentId: {}", data.getContentId());
            } catch (Exception e) {
                log.error("Failed to update label detail for contentId: {}",
                        data.getContentId(), e);
            }
        }
    }
}