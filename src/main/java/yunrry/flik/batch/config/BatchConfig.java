package yunrry.flik.batch.config;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import yunrry.flik.batch.domain.TourismRawData;
import yunrry.flik.batch.job.GooglePlacesEnrichmentJob;
import yunrry.flik.batch.job.processor.InfoDetailProcessor;
import yunrry.flik.batch.job.processor.LabelDetailProcessor;
import yunrry.flik.batch.job.processor.TourismDataProcessor;
import yunrry.flik.batch.job.reader.DetailItemReader;
import yunrry.flik.batch.job.reader.LabelDetailItemReader;
import yunrry.flik.batch.job.reader.TourismApiItemReader;
import yunrry.flik.batch.job.writer.InfoDetailWriter;
import yunrry.flik.batch.job.writer.LabelDetailWriter;
import yunrry.flik.batch.job.writer.TourismDataWriter;
import yunrry.flik.batch.listener.BatchJobListener;
import yunrry.flik.batch.migration.service.*;

@Configuration
@RequiredArgsConstructor
public class BatchConfig {

    private final LabelDetailItemReader labelDetailItemReader;
    private final LabelDetailProcessor labelDetailProcessor;
    private final LabelDetailWriter labelDetailWriter;

    private final DetailItemReader detailItemReader;
    private final InfoDetailProcessor infoDetailProcessor;
    private final InfoDetailWriter infoDetailWriter;


    private final TourismApiItemReader tourismApiItemReader;
    private final TourismDataProcessor tourismDataProcessor;
    private final TourismDataWriter tourismDataWriter;

    private final BatchJobListener batchJobListener;

    private final GooglePlacesEnrichmentJob googlePlacesEnrichmentJob;


    // 마이그레이션 서비스들
    private final AccommodationMigrationService accommodationMigrationService;
    private final CulturalFacilitiesMigrationService culturalFacilitiesMigrationService;
    private final FestivalEventsMigrationService festivalEventsMigrationService;
    private final RestaurantMigrationService restaurantMigrationService;
    private final ShoppingMigrationService shoppingMigrationService;
    private final SportsRecreationMigrationService sportsRecreationMigrationService;
    private final TouristAttractionsMigrationService touristAttractionsMigrationService;

    @Bean
    public Job tourismDataJob(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new JobBuilder("tourismDataJob", jobRepository)
                .listener(batchJobListener)
                .start(createStepForArea("52", jobRepository, transactionManager)) // 제주
                .next(detailIntroStep(jobRepository, transactionManager))//임시로 detail intro step만 실행 (나중에 지울것)
                .next(labelDetailStep(jobRepository, transactionManager))
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
    public Step detailIntroStep2(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("detailIntroStep", jobRepository)
                .<TourismRawData, TourismRawData>chunk(10, transactionManager)
                .reader(detailItemReader)
                .processor(infoDetailProcessor)
                .writer(infoDetailWriter)
                .build();
    }




    // 마이그레이션 Tasklet들
    @Bean
    public Tasklet accommodationMigrationTasklet() {
        return (contribution, chunkContext) -> {
            boolean success = accommodationMigrationService.migrateAccommodations();
            return success ? RepeatStatus.FINISHED : RepeatStatus.CONTINUABLE;
        };
    }

    @Bean
    public Tasklet culturalMigrationTasklet() {
        return (contribution, chunkContext) -> {
            boolean success = culturalFacilitiesMigrationService.migrateCulturalFacilities();
            return success ? RepeatStatus.FINISHED : RepeatStatus.CONTINUABLE;
        };
    }

    @Bean
    public Tasklet festivalMigrationTasklet() {
        return (contribution, chunkContext) -> {
            boolean success = festivalEventsMigrationService.migrateFestivalEvents();
            return success ? RepeatStatus.FINISHED : RepeatStatus.CONTINUABLE;
        };
    }

    @Bean
    public Tasklet restaurantMigrationTasklet() {
        return (contribution, chunkContext) -> {
            boolean success = restaurantMigrationService.migrateRestaurants();
            return success ? RepeatStatus.FINISHED : RepeatStatus.CONTINUABLE;
        };
    }

    @Bean
    public Tasklet shoppingMigrationTasklet() {
        return (contribution, chunkContext) -> {
            boolean success = shoppingMigrationService.migrateShopping();
            return success ? RepeatStatus.FINISHED : RepeatStatus.CONTINUABLE;
        };
    }

    @Bean
    public Tasklet sportsMigrationTasklet() {
        return (contribution, chunkContext) -> {
            boolean success = sportsRecreationMigrationService.migrateSportsRecreation();
            return success ? RepeatStatus.FINISHED : RepeatStatus.CONTINUABLE;
        };
    }

    @Bean
    public Tasklet touristMigrationTasklet() {
        return (contribution, chunkContext) -> {
            boolean success = touristAttractionsMigrationService.migrateTouristAttractions();
            return success ? RepeatStatus.FINISHED : RepeatStatus.CONTINUABLE;
        };
    }

    // 마이그레이션 Step들
    @Bean
    public Step accommodationMigrationStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("accommodationMigrationStep", jobRepository)
                .tasklet(accommodationMigrationTasklet(), transactionManager)
                .build();
    }

    @Bean
    public Step culturalMigrationStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("culturalMigrationStep", jobRepository)
                .tasklet(culturalMigrationTasklet(), transactionManager)
                .build();
    }

    @Bean
    public Step festivalMigrationStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("festivalMigrationStep", jobRepository)
                .tasklet(festivalMigrationTasklet(), transactionManager)
                .build();
    }

    @Bean
    public Step restaurantMigrationStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("restaurantMigrationStep", jobRepository)
                .tasklet(restaurantMigrationTasklet(), transactionManager)
                .build();
    }

    @Bean
    public Step shoppingMigrationStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("shoppingMigrationStep", jobRepository)
                .tasklet(shoppingMigrationTasklet(), transactionManager)
                .build();
    }

    @Bean
    public Step sportsMigrationStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("sportsMigrationStep", jobRepository)
                .tasklet(sportsMigrationTasklet(), transactionManager)
                .build();
    }

    @Bean
    public Step touristMigrationStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("touristMigrationStep", jobRepository)
                .tasklet(touristMigrationTasklet(), transactionManager)
                .build();
    }

    // 개별 마이그레이션 Job들
    @Bean
    public Job accommodationMigrationJob(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new JobBuilder("accommodationMigrationJob", jobRepository)
                .listener(batchJobListener)
                .start(accommodationMigrationStep(jobRepository, transactionManager))
                .build();
    }

    @Bean
    public Job culturalMigrationJob(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new JobBuilder("culturalMigrationJob", jobRepository)
                .listener(batchJobListener)
                .start(culturalMigrationStep(jobRepository, transactionManager))
                .build();
    }

    @Bean
    public Job festivalMigrationJob(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new JobBuilder("festivalMigrationJob", jobRepository)
                .listener(batchJobListener)
                .start(festivalMigrationStep(jobRepository, transactionManager))
                .build();
    }

    @Bean
    public Job restaurantMigrationJob(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new JobBuilder("restaurantMigrationJob", jobRepository)
                .listener(batchJobListener)
                .start(restaurantMigrationStep(jobRepository, transactionManager))
                .build();
    }

    @Bean
    public Job shoppingMigrationJob(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new JobBuilder("shoppingMigrationJob", jobRepository)
                .listener(batchJobListener)
                .start(shoppingMigrationStep(jobRepository, transactionManager))
                .build();
    }

    @Bean
    public Job sportsMigrationJob(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new JobBuilder("sportsMigrationJob", jobRepository)
                .listener(batchJobListener)
                .start(sportsMigrationStep(jobRepository, transactionManager))
                .build();
    }

    @Bean
    public Job touristMigrationJob(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new JobBuilder("touristMigrationJob", jobRepository)
                .listener(batchJobListener)
                .start(touristMigrationStep(jobRepository, transactionManager))
                .build();
    }

    // 전체 마이그레이션 Job
    @Bean
    public Job allMigrationJob(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new JobBuilder("allMigrationJob", jobRepository)
                .listener(batchJobListener)
                .start(touristMigrationStep(jobRepository, transactionManager))
                .next(accommodationMigrationStep(jobRepository, transactionManager))
                .next(culturalMigrationStep(jobRepository, transactionManager))
                .next(festivalMigrationStep(jobRepository, transactionManager))
                .next(restaurantMigrationStep(jobRepository, transactionManager))
                .next(shoppingMigrationStep(jobRepository, transactionManager))
                .next(sportsMigrationStep(jobRepository, transactionManager))
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
    public Job detailIntroOnlyJob2(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new JobBuilder("detailIntroOnlyJob2", jobRepository)
                .listener(batchJobListener)
                .start(detailIntroStep2(jobRepository, transactionManager))
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

    @Bean
    public Step labelDetailStep2(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("labelDetailStep2", jobRepository)
                .<TourismRawData, TourismRawData>chunk(10, transactionManager)
                .reader(labelDetailItemReader)
                .processor(labelDetailProcessor.labelDetailProcessor2())
                .writer(labelDetailWriter)
                .build();
    }

    @Bean
    public Job labelDetailJob2(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new JobBuilder("labelDetailJob2", jobRepository)
                .listener(batchJobListener)
                .start(labelDetailStep2(jobRepository, transactionManager))
                .build();
    }


    @Bean
    public Job areaBasedTourismJob(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new JobBuilder("areaBasedTourismJob", jobRepository)
                .start(createStepForArea("39", jobRepository, transactionManager)) // 제주
                .next(createStepForArea("6", jobRepository, transactionManager))  // 부산
                .build();
    }

    private Step createStepForArea(String areaCode, JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        // Step 실행 전에 reader에 지역 코드 설정
        tourismApiItemReader.setAreaCode(areaCode);

        return new StepBuilder("tourismStep_" + areaCode, jobRepository)
                .<TourismRawData, TourismRawData>chunk(50, transactionManager)
                .reader(tourismApiItemReader)
                .processor(tourismDataProcessor)
                .writer(tourismDataWriter)
                .build();
    }



}