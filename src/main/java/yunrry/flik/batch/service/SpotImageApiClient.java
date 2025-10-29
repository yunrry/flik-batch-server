// java
package yunrry.flik.batch.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SpotImageApiClient {

    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${tourism-api.service-key}")
    private String serviceKey;

    public List<String> fetchOriginImageUrls(String contentId) {
        try {
            // 1차: subImageYN 포함 호출
            return doFetch(contentId, true);
        } catch (Exception e) {
            log.error("이미지 API 호출 실패 contentId={}", contentId, e);
            return List.of();
        }
    }

    private List<String> doFetch(String contentId, boolean includeSubImage) throws Exception {
        URI uri = buildUri(contentId, includeSubImage);
        ResponseEntity<String> resp = restTemplate.getForEntity(uri, String.class);
        if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null) {
            log.warn("API 응답 실패 contentId={}, status={}", contentId, resp.getStatusCode());
            return List.of();
        }

        String bodyText = resp.getBody();
        JsonNode root = objectMapper.readTree(bodyText);

        // 플랫 오류 스키마 처리
        if (root.has("resultCode") && !"0000".equals(root.path("resultCode").asText("0000"))) {
            String code = root.path("resultCode").asText();
            String msg = root.path("resultMsg").asText();
            if (includeSubImage && msg != null && msg.contains("subImageYN")) {
                log.info("subImageYN 미지원으로 재시도: contentId={}", contentId);
                return doFetch(contentId, false);
            }
            log.warn("API 오류: contentId={}, resultCode={}, resultMsg={}", contentId, code, msg);
            return List.of();
        }

        // 정상 래핑 스키마 처리
        JsonNode response = root.path("response");
        if (!response.isMissingNode() && !response.isNull()) {
            JsonNode header = response.path("header");
            String resultCode = header.path("resultCode").asText("0000");
            String resultMsg = header.path("resultMsg").asText("");

            if (!"0000".equals(resultCode)) {
                if (includeSubImage && resultMsg != null && resultMsg.contains("subImageYN")) {
                    log.info("subImageYN 미지원으로 재시도: contentId={}", contentId);
                    return doFetch(contentId, false);
                }
                log.warn("API 오류: contentId={}, resultCode={}, resultMsg={}", contentId, resultCode, resultMsg);
                return List.of();
            }

            JsonNode body = response.path("body");
            int totalCount = body.path("totalCount").asInt(-1);
            if (totalCount == 0) {
                log.info("이미지 없음(totalCount=0): contentId={}", contentId);
                return List.of();
            }

            JsonNode itemsNode = body.path("items").path("item");
            List<String> urls = new ArrayList<>();

            if (itemsNode.isArray()) {
                for (JsonNode n : itemsNode) {
                    String url = n.path("originimgurl").asText(null);
                    if (url != null && !url.isBlank()) {
                        urls.add(url);
                    } else {
                        log.warn("originimgurl 누락 또는 공백: contentId={}, node={}", contentId, n.toString());
                    }
                }
            } else if (!itemsNode.isMissingNode() && !itemsNode.isNull()) {
                String url = itemsNode.path("originimgurl").asText(null);
                if (url != null && !url.isBlank()) {
                    urls.add(url);
                } else {
                    log.warn("originimgurl 누락 또는 공백(단일 항목): contentId={}, node={}", contentId, itemsNode.toString());
                }
            } else {
                String snippet = bodyText.length() > 512 ? bodyText.substring(0, 512) + "..." : bodyText;
                log.warn("items 노드가 없음: contentId={}, bodySnippet={}", contentId, snippet);
            }

            return urls;
        }

        // 스키마 불일치(예: 플랫 에러 메시지) 폴백
        if (includeSubImage && bodyText.contains("INVALID_REQUEST_PARAMETER_ERROR(subImageYN)")) {
            log.info("subImageYN 미지원으로 재시도: contentId={}", contentId);
            return doFetch(contentId, false);
        }

        String snippet = bodyText.length() > 512 ? bodyText.substring(0, 512) + "..." : bodyText;
        log.warn("응답 스키마 불일치: contentId={}, bodySnippet={}", contentId, snippet);
        return List.of();
    }

    private URI buildUri(String contentId, boolean includeSubImage) {
        UriComponentsBuilder b = UriComponentsBuilder
                .fromHttpUrl("https://apis.data.go.kr/B551011/KorService2/detailImage2")
                .queryParam("MobileOS", "WEB")
                .queryParam("MobileApp", "FLIK")
                .queryParam("contentId", contentId)
                .queryParam("imageYN", "Y")
                .queryParam("numOfRows", 100)
                .queryParam("pageNo", 1)
                .queryParam("serviceKey", serviceKey)
                .queryParam("_type", "json");

        if (includeSubImage) {
            b.queryParam("subImageYN", "Y");
        }
        return b.build(true).toUri();
    }
}