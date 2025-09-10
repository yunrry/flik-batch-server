package yunrry.flik.batch.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.*;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import yunrry.flik.batch.service.NotificationService;
import yunrry.flik.batch.domain.PlaceReview;
import yunrry.flik.batch.domain.TourismRawData;
import yunrry.flik.batch.service.GooglePlacesService;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class GooglePlacesEnrichmentJob {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;
    private final GooglePlacesService googlePlacesService;
    private final NotificationService discordNotificationService;

    private static final Logger nullRatingLogger = LoggerFactory.getLogger("NULL_RATING_LOGGER");
    private static final Logger noPlaceFoundLogger = LoggerFactory.getLogger("NO_PLACE_FOUND_LOGGER");


    @Bean
    public Job googlePlacesJob() {
        return new JobBuilder("googlePlacesJob", jobRepository)
                .listener(googlePlacesJobListener())
                .start(enrichTouristAttractionsStep())
                .next(enrichRestaurantsStep())
                .next(enrichAccommodationsStep())
                .next(enrichCulturalFacilitiesStep())
                .next(enrichLeisureSportsStep())
                .next(enrichShoppingStep())
                .build();
    }


    @Bean
    public Step enrichCulturalFacilitiesStep() {
        return new StepBuilder("enrichCulturalFacilitiesStep", jobRepository)
                .<TourismRawData, TourismRawData>chunk(10, transactionManager)
                .reader(culturalFacilitiesReader())
                .processor(placesProcessor())
                .writer(culturalFacilitiesWriter())
                .build();
    }

    @Bean
    public Step enrichLeisureSportsStep() {
        return new StepBuilder("enrichLeisureSportsStep", jobRepository)
                .<TourismRawData, TourismRawData>chunk(10, transactionManager)
                .reader(leisureSportsReader())
                .processor(placesProcessor())
                .writer(leisureSportsWriter())
                .build();
    }

    @Bean
    public Step enrichShoppingStep() {
        return new StepBuilder("enrichShoppingStep", jobRepository)
                .<TourismRawData, TourismRawData>chunk(10, transactionManager)
                .reader(shoppingReader())
                .processor(placesProcessor())
                .writer(shoppingWriter())
                .build();
    }

    @Bean
    public ItemReader<TourismRawData> culturalFacilitiesReader() {
        return new JdbcCursorItemReaderBuilder<TourismRawData>()
                .name("culturalFacilitiesReader")
                .dataSource(dataSource)
                .sql("SELECT content_id, title, addr1 FROM fetched_cultural_facilities WHERE google_rating IS NULL")
                .rowMapper(this::mapRow)
                .build();
    }

    @Bean
    public ItemReader<TourismRawData> leisureSportsReader() {
        return new JdbcCursorItemReaderBuilder<TourismRawData>()
                .name("leisureSportsReader")
                .dataSource(dataSource)
                .sql("SELECT content_id, title, addr1 FROM fetched_sports_recreation WHERE google_rating IS NULL")
                .rowMapper(this::mapRow)
                .build();
    }

    @Bean
    public ItemReader<TourismRawData> shoppingReader() {
        return new JdbcCursorItemReaderBuilder<TourismRawData>()
                .name("shoppingReader")
                .dataSource(dataSource)
                .sql("SELECT content_id, title, addr1 FROM fetched_shopping WHERE google_rating IS NULL")
                .rowMapper(this::mapRow)
                .build();
    }

    @Bean
    public ItemWriter<TourismRawData> culturalFacilitiesWriter() {
        return items -> {
            for (TourismRawData item : items) {
                updateGoogleData("fetched_cultural_facilities", item);
            }
        };
    }

    @Bean
    public ItemWriter<TourismRawData> leisureSportsWriter() {
        return items -> {
            for (TourismRawData item : items) {
                updateGoogleData("fetched_sports_recreation", item);
            }
        };
    }

    @Bean
    public ItemWriter<TourismRawData> shoppingWriter() {
        return items -> {
            for (TourismRawData item : items) {
                updateGoogleData("fetched_shopping", item);
            }
        };
    }

    @Bean
    public JobExecutionListener googlePlacesJobListener() {
        return new JobExecutionListener() {
            @Override
            public void beforeJob(JobExecution jobExecution) {
                log.info("Google Places enrichment job started: {}", jobExecution.getId());
                discordNotificationService.sendGooglePlacesJobStartAlert(jobExecution);
            }

            @Override
            public void afterJob(JobExecution jobExecution) {
                discordNotificationService.sendGooglePlacesJobCompletionAlert(jobExecution);
                log.info("Google Places enrichment job finished: {} with status: {}",
                        jobExecution.getId(), jobExecution.getStatus());
            }
        };
    }

    @Bean
    public Step enrichTouristAttractionsStep() {
        return new StepBuilder("enrichTouristAttractionsStep", jobRepository)
                .<TourismRawData, TourismRawData>chunk(10, transactionManager)
                .reader(touristAttractionsReader())
                .processor(placesProcessor())
                .writer(touristAttractionsWriter())
                .build();
    }

    @Bean
    public Step enrichRestaurantsStep() {
        return new StepBuilder("enrichRestaurantsStep", jobRepository)
                .<TourismRawData, TourismRawData>chunk(10, transactionManager)
                .reader(restaurantsReader())
                .processor(placesProcessor())
                .writer(restaurantsWriter())
                .build();
    }

    @Bean
    public Step enrichFestivalsStep() {
        return new StepBuilder("enrichFestivalsStep", jobRepository)
                .<TourismRawData, TourismRawData>chunk(10, transactionManager)
                .reader(festivalsReader())
                .processor(placesProcessor())
                .writer(festivalsWriter())
                .build();
    }

    @Bean
    public ItemReader<TourismRawData> touristAttractionsReader() {
        return new JdbcCursorItemReaderBuilder<TourismRawData>()
                .name("touristAttractionsReader")
                .dataSource(dataSource)
                .sql("SELECT content_id, title, addr1 FROM fetched_tourist_attractions WHERE google_rating IS NULL")
                .rowMapper(this::mapRow)
                .build();
    }

    @Bean
    public ItemReader<TourismRawData> restaurantsReader() {
        return new JdbcCursorItemReaderBuilder<TourismRawData>()
                .name("restaurantsReader")
                .dataSource(dataSource)
                .sql("SELECT content_id, title, addr1 FROM fetched_restaurants WHERE google_rating IS NULL")
                .rowMapper(this::mapRow)
                .build();
    }

    @Bean
    public ItemReader<TourismRawData> festivalsReader() {
        return new JdbcCursorItemReaderBuilder<TourismRawData>()
                .name("festivalsReader")
                .dataSource(dataSource)
                .sql("SELECT content_id, title, addr1 FROM fetched_festivals_events WHERE google_rating IS NULL")
                .rowMapper(this::mapRow)
                .build();
    }

    @Bean
    public ItemProcessor<TourismRawData, TourismRawData> placesProcessor() {
        return item -> {
            try {
                PlaceReview placeData = googlePlacesService.getPlaceData(item.getTitle(), item.getAddr1());

                if (placeData != null) {
                    if (placeData.getRating() != null) {
                        // 정상 처리
                        item.setGoogleRating(placeData.getRating());
                        item.setGoogleReviewCount(placeData.getReviewCount());
                        item.setGoogleReviews(placeData.getReviews());
                        item.setGooglePlaceId(placeData.getPlaceId());

                        log.debug("Success: {} | Rating: {}", item.getTitle(), placeData.getRating());
                    } else {
                        // rating이 null인 경우 별도 로거로 저장
                        nullRatingLogger.info("ContentId={}, Title='{}', Address='{}', PlaceId={}, ReviewCount={}",
                                item.getContentId(),
                                item.getTitle().replace(",", "\\,"), // CSV 안전을 위해 쉼표 이스케이프
                                item.getAddr1().replace(",", "\\,"),
                                placeData.getPlaceId(),
                                placeData.getReviewCount());

                        item.setGooglePlaceId(placeData.getPlaceId());
                        item.setGoogleReviewCount(placeData.getReviewCount());
                        item.setGoogleReviews(placeData.getReviews());
                    }
                } else {
                    // 장소를 찾지 못한 경우 별도 로거로 저장
                    noPlaceFoundLogger.info("ContentId={}, Title='{}', Address='{}'",
                            item.getContentId(),
                            item.getTitle().replace(",", "\\,"),
                            item.getAddr1().replace(",", "\\,"));
                }

                return item;

            } catch (Exception e) {
                log.error("API Error for {}: {}", item.getContentId(), e.getMessage());
                return item;
            }
        };
    }

    @Bean
    public ItemWriter<TourismRawData> touristAttractionsWriter() {
        return items -> {
            for (TourismRawData item : items) {
                updateGoogleData("fetched_tourist_attractions", item);
            }
        };
    }

    @Bean
    public ItemWriter<TourismRawData> restaurantsWriter() {
        return items -> {
            for (TourismRawData item : items) {
                updateGoogleData("fetched_restaurants", item);
            }
        };
    }

    @Bean
    public ItemWriter<TourismRawData> festivalsWriter() {
        return items -> {
            for (TourismRawData item : items) {
                updateGoogleData("fetched_festivals_events", item);
            }
        };
    }

    private TourismRawData mapRow(ResultSet rs, int rowNum) throws SQLException {
        return TourismRawData.builder()
                .contentId(rs.getString("content_id"))
                .title(rs.getString("title"))
                .addr1(rs.getString("addr1"))
                .build();
    }

    private void updateGoogleData(String tableName, TourismRawData item) {
        String sql = String.format("""
            UPDATE %s SET 
                google_place_id = ?,
                google_rating = ?,
                google_review_count = ?,
                google_reviews = ?,
                updated_at = CURRENT_TIMESTAMP
            WHERE content_id = ?
            """, tableName);

        String reviewsJson = item.getGoogleReviews() != null ?
                String.join("|||", item.getGoogleReviews()) : null;

        jdbcTemplate.update(sql,
                item.getGooglePlaceId(),
                item.getGoogleRating(),
                item.getGoogleReviewCount(),
                reviewsJson,
                item.getContentId());
    }

    @Bean
    public Step enrichAccommodationsStep() {
        return new StepBuilder("enrichAccommodationsStep", jobRepository)
                .<TourismRawData, TourismRawData>chunk(10, transactionManager)
                .reader(accommodationsReader())
                .processor(placesProcessor())
                .writer(accommodationsWriter())
                .build();
    }

    @Bean
    public ItemReader<TourismRawData> accommodationsReader() {
        return new JdbcCursorItemReaderBuilder<TourismRawData>()
                .name("accommodationsReader")
                .dataSource(dataSource)
                .sql("SELECT content_id, title, addr1 FROM fetched_accommodations WHERE google_rating IS NULL")
                .rowMapper(this::mapRow)
                .build();
    }

    @Bean
    public ItemWriter<TourismRawData> accommodationsWriter() {
        return items -> {
            for (TourismRawData item : items) {
                updateGoogleData("fetched_accommodations", item);
            }
        };
    }


}