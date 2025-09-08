package yunrry.flik.batch.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import yunrry.flik.batch.domain.PlaceReview;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class GooglePlacesService {

    private final ObjectMapper objectMapper;
    private final WebClient webClient = WebClient.builder()
            .baseUrl("https://maps.googleapis.com/maps/api/place")
            .build();

    @Value("${google.places.api-key}")
    private String apiKey;

    public PlaceReview getPlaceData(String title, String address) {
        try {
            // 1. Text Search로 장소 찾기
            String placeId = searchPlace(title, address);
            if (placeId == null) return null;

            // 2. Place Details로 별점 및 리뷰 가져오기
            return getPlaceDetails(placeId);
        } catch (Exception e) {
            log.error("Error fetching Google Places data for: {} {}", title, address, e);
            return null;
        }
    }

    private String searchPlace(String title, String address) {
        String query = title + " " + address;
        Map<String, Object> params = Map.of(
                "query", query,
                "key", apiKey,
                "language", "ko"
        );

        try {
            String response = webClient.get()
                    .uri(uriBuilder -> {
                        uriBuilder.path("/textsearch/json");
                        params.forEach(uriBuilder::queryParam);
                        return uriBuilder.build();
                    })
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(10))
                    .block();

            JsonNode root = objectMapper.readTree(response);
            JsonNode results = root.path("results");

            if (results.isArray() && results.size() > 0) {
                return results.get(0).path("place_id").asText();
            }
            return null;
        } catch (Exception e) {
            log.error("Error searching place: {}", query, e);
            return null;
        }
    }

    private PlaceReview getPlaceDetails(String placeId) {
        Map<String, Object> params = Map.of(
                "place_id", placeId,
                "fields", "rating,reviews,user_ratings_total",
                "key", apiKey,
                "language", "ko"
        );

        try {
            String response = webClient.get()
                    .uri(uriBuilder -> {
                        uriBuilder.path("/details/json");
                        params.forEach(uriBuilder::queryParam);
                        return uriBuilder.build();
                    })
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(10))
                    .block();

            return parsePlaceDetails(response, placeId);
        } catch (Exception e) {
            log.error("Error getting place details for: {}", placeId, e);
            return null;
        }
    }

    private PlaceReview parsePlaceDetails(String response, String placeId) {
        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode result = root.path("result");

            double rating = result.path("rating").asDouble(0.0);
            int reviewCount = result.path("user_ratings_total").asInt(0);

            List<String> reviews = new ArrayList<>();
            JsonNode reviewsNode = result.path("reviews");

            if (reviewsNode.isArray()) {
                for (int i = 0; i < Math.min(10, reviewsNode.size()); i++) {
                    JsonNode review = reviewsNode.get(i);
                    String text = review.path("text").asText();
                    if (!text.isEmpty()) {
                        reviews.add(text);
                    }
                }
            }

            return PlaceReview.builder()
                    .placeId(placeId)
                    .rating(rating)
                    .reviewCount(reviewCount)
                    .reviews(reviews)
                    .build();

        } catch (Exception e) {
            log.error("Error parsing place details", e);
            return null;
        }
    }
}