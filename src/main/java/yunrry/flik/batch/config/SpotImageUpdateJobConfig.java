package yunrry.flik.batch.config;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import yunrry.flik.batch.domain.SpotImageRecord;
import yunrry.flik.batch.domain.SpotImageUpdate;
import yunrry.flik.batch.job.processor.SpotImageUpdateProcessor;
import yunrry.flik.batch.job.reader.SpotImageRepositoryItemReader;
import yunrry.flik.batch.listener.BatchJobListener;
import yunrry.flik.batch.repository.SpotImageRepository;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class SpotImageUpdateJobConfig {

    private final SpotImageRepository spotImageRepository;
    private final SpotImageUpdateProcessor processor;
    private final BatchJobListener batchJobListener;

    @Bean
    public ItemStreamReader<SpotImageRecord> spotImageReader() {
        return new SpotImageRepositoryItemReader(spotImageRepository, 200);
    }

    @Bean
    public org.springframework.batch.item.ItemWriter<SpotImageUpdate> spotImageWriter() {
        return items -> {
            if (items == null || items.isEmpty()) return;
            spotImageRepository.updateImageUrls(items.getItems());
        };
    }

    @Bean
    public Step spotImageUpdateStep(JobRepository jobRepository, PlatformTransactionManager txManager) {
        return new StepBuilder("spotImageUpdateStep", jobRepository)
                .<SpotImageRecord, SpotImageUpdate>chunk(50, txManager)
                .reader(spotImageReader())
                .processor(processor)
                .writer(spotImageWriter())
                .build();
    }

    @Bean
    public Job spotImageBackfillJob(JobRepository jobRepository, PlatformTransactionManager txManager) {
        return new JobBuilder("spotImageBackfillJob", jobRepository)
                .listener(batchJobListener)
                .start(spotImageUpdateStep(jobRepository, txManager))
                .build();
    }
}