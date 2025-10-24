package yunrry.flik.batch.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import yunrry.flik.batch.domain.TourismRawData;
import yunrry.flik.batch.exception.ApiLimitExceededException;
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
            .baseUrl("https://apis.data.go.kr/B551011/KorService2")
            .defaultHeader("accept", "*/*")
            .build();



    // ApiService.java
    public List<TourismRawData> fetchAreaBasedListByContentType(int pageNo, String areaCode,
                                                                String contentTypeId, int numOfRows, String serviceKeyParam) {
        Map<String, Object> params = Map.of(
                "serviceKey", serviceKeyParam,
                "numOfRows", numOfRows,
                "pageNo", pageNo,
                "MobileOS", "WEB",
                "MobileApp", "Flik",
                "contentTypeId", contentTypeId,
                "_type", "json",
                "arrange", "C",
                "areaCode", areaCode
        );

        try {
            log.info("Calling API with params: {}", params);
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
            log.info("API response length: {}, first 100 chars: {}",
                    response.length(), response.substring(0, Math.min(100, response.length())));
            handleApiResponse(response);
            return parseAreaBasedResponse(response);
        } catch (Exception e) {
            log.error("Error fetching area based list", e);
            return Collections.emptyList();
        }
    }

    private void handleApiResponse(String response) throws ApiLimitExceededException {
        if (response != null && response.contains("LIMITED_NUMBER_OF_SERVICE_REQUESTS_EXCEEDS_ERROR")) {
            throw new ApiLimitExceededException("API 호출 한도 초과");
        }
    }


    // ApiService.java
    public List<TourismRawData> fetchAreaBasedList(int pageNo, String areaCode, String serviceKeyParam) {
        Map<String, Object> params = Map.of(
                "serviceKey", serviceKeyParam,
                "numOfRows", 100,
                "pageNo", pageNo,
                "MobileOS", "WEB",
                "MobileApp", "Flik",
                "contentTypeId", 39, // 음식점
                "_type", "json",
                "arrange", "R",
                "areaCode", areaCode
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
            handleApiResponse(response);
            return parseAreaBasedResponse(response);
        } catch (Exception e) {
            log.error("Error fetching area based list", e);
            return Collections.emptyList();
        }
    }


    public Map<String, Object> fetchDetailIntro(String contentId, String contentTypeId, String serviceKeyParam) {
        Map<String, Object> params = Map.of(
                "serviceKey", serviceKeyParam,
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
            handleApiResponse(response);
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
                    // 여행코스(25) 필터링
                    String contentTypeId = getTextValue(item, "contenttypeid");
                    if ("25".equals(contentTypeId)) {
                        log.debug("Skipping travel course data: contentId={}", getTextValue(item, "contentid"));
                        continue;
                    }

                    TourismRawData data = parseAreaBasedItem(item);
                    if (data != null) {
                        results.add(data);
                    }
                }
            } else {
                // 단일 아이템인 경우도 체크
                String contentTypeId = getTextValue(itemArray, "contenttypeid");
                if (!"25".equals(contentTypeId)) {
                    TourismRawData data = parseAreaBasedItem(itemArray);
                    if (data != null) {
                        results.add(data);
                    }
                }
            }

            log.info("Parsed {} items (filtered out travel courses)", results.size());
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




    public TourismRawData fetchDetailCommon(String contentId, String serviceKeyParam) {
        Map<String, Object> params = Map.of(
                "serviceKey", serviceKeyParam,
                "contentId", contentId,
                "MobileOS", "WEB",
                "MobileApp", "Flik",
                "_type", "json"

        );

        try {
            String response = webClient.get()
                    .uri(uriBuilder -> {
                        uriBuilder.path("/detailCommon2");
                        params.forEach(uriBuilder::queryParam);
                        return uriBuilder.build();
                    })
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(30))
                    .block();

            return parseDetailCommonResponse(response, contentId);

        } catch (WebClientResponseException e) {
            log.error("API call failed for contentId: {}, status={}, body={}",
                    contentId, e.getStatusCode(), e.getResponseBodyAsString());
            return null;
        } catch (Exception e) {
            log.error("Error fetching detail common for contentId: {}", contentId, e);
            return null;
        }
    }

    private String buildDetailCommonUrl(String contentId) {
        String url = String.format(
                "%s/detailCommon2?serviceKey=%s&contentId=%s&MobileOS=ETC&MobileApp=flik&_type=json",
                baseUrl, serviceKey, contentId
        );
        log.debug("Built detail common URL: {}", url);
        return url;
    }

    private TourismRawData parseDetailCommonResponse(String response, String contentId) {
        try {
            if (response == null || response.trim().isEmpty()) {
                log.warn("Empty response for contentId: {}", contentId);
                return null;
            }

            JsonNode root = objectMapper.readTree(response);
            JsonNode header = root.path("response").path("header");

            if (!"0000".equals(header.path("resultCode").asText())) {
                log.warn("API error for contentId: {}, message: {}",
                        contentId, header.path("resultMsg").asText());
                return null;
            }

            JsonNode items = root.path("response").path("body").path("items");
            if (items.isMissingNode() || !items.has("item")) {
                log.warn("No items found for contentId: {}", contentId);
                return null;
            }

            JsonNode item = items.path("item");
            if (item.isArray() && item.size() > 0) {
                item = item.get(0);
            }

            // 분류 코드 추출
            String lclsSystm1 = getTextValue(item, "lclsSystm1");
            String lclsSystm2 = getTextValue(item, "lclsSystm2");
            String lclsSystm3 = getTextValue(item, "lclsSystm3");

            // DB에서 분류명 조회
            Map<String, String> labelNames = tourismDataRepository.findLabelNames(lclsSystm1, lclsSystm2, lclsSystm3);

            return TourismRawData.builder()
                    .contentId(contentId)
                    .contentTypeId(getTextValue(item, "contenttypeid"))
                    .overview(getTextValue(item, "overview"))
                    .labelDepth1(labelNames.get("depth1"))
                    .labelDepth2(labelNames.get("depth2"))
                    .labelDepth3(labelNames.get("depth3"))
                    .build();

        } catch (Exception e) {
            log.error("Error parsing detail common response for contentId: {}", contentId, e);
            return null;
        }
    }
}