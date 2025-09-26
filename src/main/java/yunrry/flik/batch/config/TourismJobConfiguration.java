package yunrry.flik.batch.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import yunrry.flik.batch.domain.TourismRawData;
import yunrry.flik.batch.job.processor.DynamicTourismProcessor;
import yunrry.flik.batch.job.processor.TouristSpotProcessor;
import yunrry.flik.batch.job.reader.DynamicTourismReader;
import yunrry.flik.batch.job.reader.TouristSpotItemReader;
import yunrry.flik.batch.job.writer.DynamicTourismWriter;
import yunrry.flik.batch.job.writer.TouristSpotWriter;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class TourismJobConfiguration {

    private final DynamicTourismReader dynamicTourismReader;
    private final DynamicTourismProcessor dynamicTourismProcessor;
    private final DynamicTourismWriter dynamicTourismWriter;


    @Bean
    public Job tourismDataCollectionJob(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new JobBuilder("tourismDataCollectionJob", jobRepository)
                .start(tourismCollectionStep(jobRepository, transactionManager))
                .build();
    }

    @Bean
    public Step tourismCollectionStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("tourismCollectionStep", jobRepository)
                .<TourismRawData, TourismRawData>chunk(10, transactionManager)
                .reader(dynamicTourismReader)  // contentTypeId 파라미터로 동적 처리
                .processor(dynamicTourismProcessor)  // contentTypeId 파라미터로 동적 처리
                .writer(dynamicTourismWriter)
                .build();
    }
}