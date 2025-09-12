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
import yunrry.flik.batch.job.GooglePlacesEnrichmentJob;
import yunrry.flik.batch.job.processor.TourismDataProcessor;
import yunrry.flik.batch.job.reader.DetailItemReader;
import yunrry.flik.batch.job.reader.LabelDetailItemReader;
import yunrry.flik.batch.job.reader.TourismApiItemReader;
import yunrry.flik.batch.job.writer.TourismDataWriter;
import yunrry.flik.batch.listener.BatchJobListener;

@Configuration
@RequiredArgsConstructor
public class BatchConfig {

    private final LabelDetailItemReader labelDetailItemReader;
    private final TourismDataProcessor labelDetailProcessor;
    private final TourismDataWriter labelDetailWriter;
    private final DetailItemReader detailItemReader;
    private final TourismApiItemReader tourismApiItemReader;
    private final TourismDataProcessor tourismDataProcessor;
    private final TourismDataWriter tourismDataWriter;
    private final BatchJobListener batchJobListener;

    private final GooglePlacesEnrichmentJob googlePlacesEnrichmentJob;

    @Bean
    public Job tourismDataJob(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new JobBuilder("tourismDataJob", jobRepository)
                .listener(batchJobListener)
                .start(detailIntroStep(jobRepository, transactionManager))//임시로 detail intro step만 실행 (나중에 지울것)
                .next(labelDetailStep(jobRepository, transactionManager))
//                .start(areaBasedListStep(jobRepository, transactionManager))
//                .next(detailIntroStep(jobRepository, transactionManager))
//                .next(googlePlacesEnrichmentJob.enrichTouristAttractionsStep())
//                .next(googlePlacesEnrichmentJob.enrichRestaurantsStep())
//                .next(googlePlacesEnrichmentJob.enrichAccommodationsStep())
//                .next(googlePlacesEnrichmentJob.enrichCulturalFacilitiesStep())
//                .next(googlePlacesEnrichmentJob.enrichLeisureSportsStep())
//                .next(googlePlacesEnrichmentJob.enrichShoppingStep())
                .build();
    }

    @Bean
    public Step areaBasedListStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("areaBasedListStep", jobRepository)
                .<TourismRawData, TourismRawData>chunk(50, transactionManager)
                .reader(tourismApiItemReader)
                .processor(tourismDataProcessor)
                .writer(tourismDataWriter)
                .build();
    }

    @Bean
    public Step detailIntroStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("detailIntroStep", jobRepository)
                .<TourismRawData, TourismRawData>chunk(10, transactionManager)
                .reader(detailItemReader)
                .processor(tourismDataProcessor.createDetailProcessor())
                .writer(tourismDataWriter.createDetailWriter())
                .build();
    }

    @Bean
    public Job detailIntroOnlyJob(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new JobBuilder("detailIntroOnlyJob", jobRepository)
                .listener(batchJobListener)
                .start(detailIntroStep(jobRepository, transactionManager))
                .build();
    }

    @Bean
    public Step labelDetailStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("labelDetailStep", jobRepository)
                .<TourismRawData, TourismRawData>chunk(10, transactionManager)
                .reader(labelDetailItemReader)
                .processor(labelDetailProcessor)
                .writer(labelDetailWriter)
                .build();
    }

    @Bean
    public Job labelDetailJob(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new JobBuilder("labelDetailJob", jobRepository)
                .listener(batchJobListener)
                .start(labelDetailStep(jobRepository, transactionManager))
                .build();
    }
}