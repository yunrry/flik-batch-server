package yunrry.flik.batch.job.writer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;
import yunrry.flik.batch.domain.TourismRawData;
import yunrry.flik.batch.repository.TourismDataRepository;


@Slf4j
@Component
@RequiredArgsConstructor
public class DynamicTourismWriter implements ItemWriter<TourismRawData> {

    private final TourismDataRepository tourismDataRepository;

    private String contentTypeId;
    private String areaCode;

    @BeforeStep
    public void beforeStep(StepExecution stepExecution) {
        JobParameters jobParameters = stepExecution.getJobParameters();
        this.contentTypeId = jobParameters.getString("contentTypeId");
        this.areaCode = jobParameters.getString("areaCode");
    }

    @Override
    public void write(Chunk<? extends TourismRawData> chunk) throws Exception {
        int successCount = 0;

        for (TourismRawData item : chunk) {
            try {
                tourismDataRepository.saveAreaBasedData(item);
                tourismDataRepository.updateDetailData(item);
                tourismDataRepository.updateLabelDetailData(item);
                successCount++;
            } catch (Exception e) {
                tourismDataRepository.rollbackApiCallHistory(contentTypeId, areaCode);
                throw e;
            }
        }

        log.info("Write completed - Success: {}, Total: {}", successCount, chunk.size());
    }
}