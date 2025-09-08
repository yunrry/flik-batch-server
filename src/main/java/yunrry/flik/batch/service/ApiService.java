package yunrry.flik.batch.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import yunrry.flik.batch.domain.TourismRawData;
import yunrry.flik.batch.repository.TourismDataRepository;

import java.time.Duration;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApiService {

    private final ObjectMapper objectMapper;
    private final TourismDataRepository tourismDataRepository;

    @Value("${tourism-api.base-url}")
    private String baseUrl;

    @Value("${tourism-api.service-key}")
    private String serviceKey;

    private final WebClient webClient = WebClient.builder()
            .baseUrl("http://apis.data.go.kr/B551011/KorService2")
            .build();

    public List<TourismRawData> fetchAreaBasedList(int pageNo) {
        Map<String, Object> params = Map.of(
                "serviceKey", serviceKey,
                "numOfRows", 100,
                "pageNo", pageNo,
                "MobileOS", "WEB",
                "MobileApp", "Flik",
                "_type", "json",
                "arrange", "R",  // 생성일순
                "areaCode", "1"  // 서울
        );

        try {
            String response = webClient.get()
                    .uri(uriBuilder -> {
                        uriBuilder.path("/areaBasedList2");
                        params.forEach(uriBuilder::queryParam);
                        return uriBuilder.build();
                    })
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(30))
                    .block();

            return parseAreaBasedResponse(response);
        } catch (WebClientResponseException e) {
            log.error("API call failed: status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString());
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("Error fetching area based list", e);
            return Collections.emptyList();
        }
    }

    public Map<String, Object> fetchDetailIntro(String contentId, String contentTypeId) {
        Map<String, Object> params = Map.of(
                "serviceKey", serviceKey,
                "contentId", contentId,
                "contentTypeId", contentTypeId,
                "MobileOS", "WEB",
                "MobileApp", "Flik",
                "_type", "json"
        );

        try {
            String response = webClient.get()
                    .uri(uriBuilder -> {
                        uriBuilder.path("/detailIntro2");
                        params.forEach(uriBuilder::queryParam);
                        return uriBuilder.build();
                    })
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(30))
                    .block();

            return parseDetailIntroResponse(response);
        } catch (Exception e) {
            log.error("Error fetching detail intro for contentId: {}", contentId, e);
            return Collections.emptyMap();
        }
    }

    public List<TourismRawData> fetchUnprocessedDataForDetail() {
        return tourismDataRepository.findUnprocessedForDetail();
    }

    private List<TourismRawData> parseAreaBasedResponse(String response) {
        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode items = root.path("response").path("body").path("items");

            if (items.isMissingNode() || !items.has("item")) {
                return Collections.emptyList();
            }

            JsonNode itemArray = items.path("item");
            List<TourismRawData> results = new ArrayList<>();

            if (itemArray.isArray()) {
                for (JsonNode item : itemArray) {
                    TourismRawData data = parseAreaBasedItem(item);
                    if (data != null) {
                        results.add(data);
                    }
                }
            } else {
                TourismRawData data = parseAreaBasedItem(itemArray);
                if (data != null) {
                    results.add(data);
                }
            }

            return results;
        } catch (Exception e) {
            log.error("Error parsing area based response", e);
            return Collections.emptyList();
        }
    }

    private TourismRawData parseAreaBasedItem(JsonNode item) {
        try {
            return TourismRawData.builder()
                    .contentId(getTextValue(item, "contentid"))
                    .contentTypeId(getTextValue(item, "contenttypeid"))
                    .title(getTextValue(item, "title"))
                    .addr1(getTextValue(item, "addr1"))
                    .addr2(getTextValue(item, "addr2"))
                    .firstImage(getTextValue(item, "firstimage"))
                    .firstImage2(getTextValue(item, "firstimage2"))
                    .mapX(getTextValue(item, "mapx"))
                    .mapY(getTextValue(item, "mapy"))
                    .areaCode(getTextValue(item, "areacode"))
                    .sigunguCode(getTextValue(item, "sigungucode"))
                    .cat1(getTextValue(item, "cat1"))
                    .cat2(getTextValue(item, "cat2"))
                    .cat3(getTextValue(item, "cat3"))
                    .createdTime(getTextValue(item, "createdtime"))
                    .modifiedTime(getTextValue(item, "modifiedtime"))
                    .tel(getTextValue(item, "tel"))
                    .zipcode(getTextValue(item, "zipcode"))
                    .overview(getTextValue(item, "overview"))
                    .rawData(convertToMap(item))
                    .build();
        } catch (Exception e) {
            log.error("Error parsing area based item", e);
            return null;
        }
    }

    private Map<String, Object> parseDetailIntroResponse(String response) {
        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode items = root.path("response").path("body").path("items");

            if (items.isMissingNode() || !items.has("item")) {
                return Collections.emptyMap();
            }

            JsonNode item = items.path("item");
            if (item.isArray() && item.size() > 0) {
                item = item.get(0);
            }

            return convertToMap(item);
        } catch (Exception e) {
            log.error("Error parsing detail intro response", e);
            return Collections.emptyMap();
        }
    }

    private String getTextValue(JsonNode node, String fieldName) {
        JsonNode field = node.path(fieldName);
        return field.isMissingNode() ? "" : field.asText();
    }

    private Map<String, Object> convertToMap(JsonNode node) {
        try {
            return objectMapper.convertValue(node, Map.class);
        } catch (Exception e) {
            log.error("Error converting JsonNode to Map", e);
            return Collections.emptyMap();
        }
    }
}