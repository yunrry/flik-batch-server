package yunrry.flik.batch.job.reader;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemReader;
import org.springframework.stereotype.Component;
import yunrry.flik.batch.domain.TourismRawData;
import yunrry.flik.batch.repository.TourismDataRepository;

import java.util.List;

@RequiredArgsConstructor
@Component
public class DetailItemReader implements ItemReader<TourismRawData> {
    private final TourismDataRepository repository;
    private List<TourismRawData> unprocessedData;
    private int currentIndex = 0;

    @Override
    public TourismRawData read() throws Exception {
        if (unprocessedData == null) {
            unprocessedData = repository.findUnprocessedForDetail();
        }

        if (currentIndex >= unprocessedData.size()) {
            return null;
        }

        return unprocessedData.get(currentIndex++);
    }
}