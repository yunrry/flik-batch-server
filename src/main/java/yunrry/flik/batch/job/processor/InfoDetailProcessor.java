package yunrry.flik.batch.job.processor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;
import yunrry.flik.batch.domain.TourismRawData;
import yunrry.flik.batch.mapper.FieldMapper;
import yunrry.flik.batch.service.ApiSecondService;
import yunrry.flik.batch.service.ApiService;

import java.util.Map;


@RequiredArgsConstructor
@Component
@Slf4j
public class InfoDetailProcessor implements ItemProcessor<TourismRawData, TourismRawData> {

    private final ApiSecondService apiService;
    private final FieldMapper fieldMapper;
    private int processedCount = 0;
    private static final int MAX_PROCESS_COUNT = 1000;

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
}