package yunrry.flik.batch.job.reader;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemReader;
import org.springframework.stereotype.Component;
import yunrry.flik.batch.domain.TourismRawData;
import yunrry.flik.batch.repository.TourismDataRepository;

import java.util.List;

@RequiredArgsConstructor
@Component
@Slf4j
public class LabelDetailItemReader implements ItemReader<TourismRawData> {

    private final TourismDataRepository tourismDataRepository;
    private List<TourismRawData> data;
    private int currentIndex = 0;
    private static final int MAX_ITEMS = 1000; // 최대 처리 개수 제한

    @PostConstruct
    public void initialize() {
        log.info("LabelDetailItemReader initialized. Max items: {}", MAX_ITEMS);
        data = tourismDataRepository.findUnprocessedForLabelDetail();
        if (data.size() > MAX_ITEMS) {
            data = data.subList(0, MAX_ITEMS);
        }
        log.info("Loaded {} items for label detail processing", data.size());
    }

    @Override
    public TourismRawData read() throws Exception {
        if (data == null || currentIndex >= data.size()) {
            return null;
        }

        TourismRawData item = data.get(currentIndex);
        currentIndex++;

        log.debug("Reading item {}/{}: contentId={}",
                currentIndex, data.size(), item.getContentId());

        return item;
    }
}