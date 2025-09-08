package yunrry.flik.batch.config;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import yunrry.flik.batch.domain.TourismRawData;
import yunrry.flik.batch.job.processor.TourismDataProcessor;
import yunrry.flik.batch.job.reader.TourismApiItemReader;
import yunrry.flik.batch.job.writer.TourismDataWriter;

@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
public class BatchConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final TourismApiItemReader tourismApiItemReader;
    private final TourismDataProcessor tourismDataProcessor;
    private final TourismDataWriter tourismDataWriter;

    @Bean
    public Job tourismDataJob() {
        return new JobBuilder("tourismDataJob", jobRepository)
                .start(areaBasedListStep())
                .next(detailIntroStep())
                .build();
    }

    @Bean
    public Step areaBasedListStep() {
        return new StepBuilder("areaBasedListStep", jobRepository)
                .<TourismRawData, TourismRawData>chunk(50, transactionManager)
                .reader(tourismApiItemReader)
                .processor(tourismDataProcessor)
                .writer(tourismDataWriter)
                .build();
    }

    @Bean
    public Step detailIntroStep() {
        return new StepBuilder("detailIntroStep", jobRepository)
                .<TourismRawData, TourismRawData>chunk(10, transactionManager)
                .reader(tourismApiItemReader.createDetailReader())
                .processor(tourismDataProcessor.createDetailProcessor())
                .writer(tourismDataWriter.createDetailWriter())
                .build();
    }
}