package yunrry.flik.batch.job.processor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;
import yunrry.flik.batch.domain.TourismRawData;
import yunrry.flik.batch.service.ApiService;
import yunrry.flik.batch.service.ClassificationMappingService;

@RequiredArgsConstructor
@Component
@Slf4j
public class LabelDetailProcessor implements ItemProcessor<TourismRawData, TourismRawData> {

    private final ApiService apiService;
    private int processedCount = 0;
    private static final int MAX_PROCESS_COUNT = 1000;

    @Override
    public TourismRawData process(TourismRawData item) throws Exception {
        if (processedCount >= MAX_PROCESS_COUNT) {
            log.info("Reached maximum process count: {}", MAX_PROCESS_COUNT);
            return null;
        }

        processedCount++;
        log.debug("Processing item {}/{}: contentId={}",
                processedCount, MAX_PROCESS_COUNT, item.getContentId());

        // detailCommon API 호출하여 완전한 데이터 반환
        TourismRawData detailCommonData = apiService.fetchDetailCommon(item.getContentId());

        if (detailCommonData == null) {
            log.warn("Failed to fetch detail common for contentId: {}", item.getContentId());
            return null;
        }

        // 기존 item의 contentTypeId 유지
        return TourismRawData.builder()
                .contentId(item.getContentId())
                .contentTypeId(item.getContentTypeId())
                .overview(detailCommonData.getOverview())
                .labelDepth1(detailCommonData.getLabelDepth1())
                .labelDepth2(detailCommonData.getLabelDepth2())
                .labelDepth3(detailCommonData.getLabelDepth3())
                .build();
    }
}