package yunrry.flik.batch.job.processor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;
import yunrry.flik.batch.domain.TourismRawData;
import yunrry.flik.batch.mapper.FieldMapper;
import yunrry.flik.batch.service.ApiService;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class TourismDataProcessor implements ItemProcessor<TourismRawData, TourismRawData> {

    private final ApiService apiService;
    private final FieldMapper fieldMapper;

    @Override
    public TourismRawData process(TourismRawData item) throws Exception {
        try {
            // 기본 데이터 검증
            if (!isValidData(item)) {
                log.warn("Invalid data skipped: contentId={}, title={}", item.getContentId(), item.getTitle());
                return null;
            }

            // 컨텐츠 타입별 매핑
            item.setContentTypeName(fieldMapper.getContentTypeName(item.getContentTypeId()));

            // 수집 시간 설정
            item.setCollectedAt(LocalDateTime.now());

            // 소스 정보 설정
            item.setSource("http://apis.data.go.kr/B551011/KorService2");

            log.debug("Processed item: contentId={}, type={}", item.getContentId(), item.getContentTypeName());
            return item;
        } catch (Exception e) {
            log.error("Processing failed: contentId={}, error={}", item.getContentId(), e.getMessage());
            return null;
        }
    }

    public ItemProcessor<TourismRawData, TourismRawData> createDetailProcessor() {
        return new ItemProcessor<TourismRawData, TourismRawData>() {
            @Override
            public TourismRawData process(TourismRawData item) throws Exception {
                try {
                    // detailIntro2 API 호출
                    Map<String, Object> detailData = apiService.fetchDetailIntro(
                            item.getContentId(),
                            item.getContentTypeId()
                    );

                    // 공통 컬럼 매핑
                    fieldMapper.mapCommonFields(item, detailData);

                    // 도메인별 특화 필드 매핑
                    fieldMapper.mapDomainSpecificFields(item, detailData);

                    return item;
                } catch (Exception e) {
                    log.error("Error processing detail for contentId: {}", item.getContentId(), e);
                    return item; // 원본 데이터 유지
                }
            }
        };
    }

    private boolean isValidData(TourismRawData item) {
        return item.getContentId() != null &&
                item.getContentTypeId() != null &&
                item.getTitle() != null;
    }


}