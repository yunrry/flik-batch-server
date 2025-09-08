package yunrry.flik.batch.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
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
import yunrry.flik.batch.domain.PlaceReview;
import yunrry.flik.batch.domain.TourismRawData;
import yunrry.flik.batch.service.GooglePlacesService;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class GooglePlacesEnrichmentJob {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;
    private final GooglePlacesService googlePlacesService;

    @Bean
    public Job googlePlacesJob() {
        return new JobBuilder("googlePlacesJob", jobRepository)
                .start(enrichTouristAttractionsStep())
                .next(enrichRestaurantsStep())
                .next(enrichAccommodationsStep())
                .build();
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
                    item.setGoogleRating(placeData.getRating());
                    item.setGoogleReviewCount(placeData.getReviewCount());
                    item.setGoogleReviews(placeData.getReviews());
                    item.setGooglePlaceId(placeData.getPlaceId());
                }
                return item;
            } catch (Exception e) {
                log.error("Error processing Google Places data for: {}", item.getContentId(), e);
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