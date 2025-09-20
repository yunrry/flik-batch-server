package yunrry.flik.batch.job.reader;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.ItemReader;
import org.springframework.stereotype.Component;
import yunrry.flik.batch.domain.TourismRawData;
import yunrry.flik.batch.repository.TourismDataRepository;

import java.util.List;

@RequiredArgsConstructor
@Component
@Slf4j
public class LabelDetailItemReader implements ItemReader<TourismRawData>, StepExecutionListener {

    private final TourismDataRepository tourismDataRepository;
    private List<TourismRawData> data;
    private int currentIndex = 0;
    private static final int MAX_ITEMS = 1000; // 최대 처리 개수 제한



    @Override
    public void beforeStep(StepExecution stepExecution) {
        log.info("LabelDetailItemReader starting. Max items: {}", MAX_ITEMS);

        currentIndex = 0;

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
        return data.get(currentIndex++);
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        return ExitStatus.COMPLETED;
    }

}