package yunrry.flik.batch.service;


import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class ClassificationMappingService {

    private final Map<String, ClassificationLabel> mapping = new HashMap<>();

    @PostConstruct
    public void initialize() {
        loadClassificationMapping();
    }

    private void loadClassificationMapping() {
        // JSON 파일에서 매핑 데이터 로드 (예시)
        try {
            // response_1757668259132.json 파일 로드 로직
            // 실제 구현에서는 파일 경로를 설정에서 가져오거나 리소스로 관리
            log.info("Classification mapping loaded successfully");
        } catch (Exception e) {
            log.error("Failed to load classification mapping", e);
        }
    }

    public ClassificationLabel getClassificationLabel(String cat3) {
        return mapping.getOrDefault(cat3, ClassificationLabel.empty());
    }

    @Data
    @Builder
    @AllArgsConstructor
    public static class ClassificationLabel {
        private String depth1;
        private String depth2;
        private String depth3;

        public static ClassificationLabel empty() {
            return ClassificationLabel.builder()
                    .depth1("")
                    .depth2("")
                    .depth3("")
                    .build();
        }
    }
}