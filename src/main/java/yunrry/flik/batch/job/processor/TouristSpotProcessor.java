package yunrry.flik.batch.job.processor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;
import yunrry.flik.batch.domain.TourismRawData;
import yunrry.flik.batch.mapper.FieldMapper;
import yunrry.flik.batch.service.ApiService;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class TouristSpotProcessor implements ItemProcessor<TourismRawData, TourismRawData> {

    private final ApiService apiService;
    private final FieldMapper fieldMapper;

    private String serviceKey;
    private String areaCode;
    private static final String CONTENT_TYPE_ID = "12"; // 관광지
    private static final String CONTENT_TYPE_NAME = "관광지";

    @BeforeStep
    public void beforeStep(StepExecution stepExecution) {
        JobParameters jobParameters = stepExecution.getJobParameters();
        this.serviceKey = jobParameters.getString("serviceKey");
        this.areaCode = jobParameters.getString("areaCode");
        log.info("TouristSpotProcessor initialized - areaCode: {}, contentType: {}", areaCode, CONTENT_TYPE_NAME);
    }

    public void setServiceKey(String serviceKey) {
        this.serviceKey = serviceKey;
        log.info("Service key set externally");
    }

    @Override
    public TourismRawData process(TourismRawData item) throws Exception {
        log.debug("Processing tourist spot: contentId={}", item.getContentId());

        try {
            // 관광지 타입 검증
            if (!CONTENT_TYPE_ID.equals(item.getContentTypeId())) {
                log.debug("Skipping non-tourist spot item: contentId={}, type={}",
                        item.getContentId(), item.getContentTypeId());
                return null;
            }

            // 기본 데이터 검증
            if (!isValidTouristSpotData(item)) {
                log.warn("Invalid tourist spot data: contentId={}, title={}",
                        item.getContentId(), item.getTitle());
                return null;
            }

            // 기본 필드 설정
            item.setContentTypeName(CONTENT_TYPE_NAME);
            item.setCollectedAt(LocalDateTime.now());
            item.setSource("http://apis.data.go.kr/B551011/KorService2");

            // 1. detailIntro2 API 호출 및 매핑
            enrichWithDetailIntro(item);

            // 2. detailCommon2 API 호출 및 매핑
            enrichWithDetailCommon(item);

            log.debug("Processed complete tourist spot: contentId={}, title={}",
                    item.getContentId(), item.getTitle());

            return item;

        } catch (Exception e) {
            log.error("Error processing tourist spot: contentId={}, error={}",
                    item.getContentId(), e.getMessage(), e);
            return null;
        }
    }

    private void enrichWithDetailIntro(TourismRawData item) {
        try {
            Map<String, Object> detailData = apiService.fetchDetailIntro(
                    item.getContentId(),
                    CONTENT_TYPE_ID,
                    serviceKey
            );

            if (detailData != null && !detailData.isEmpty()) {
                fieldMapper.mapCommonFields(item, detailData);
                fieldMapper.mapDomainSpecificFields(item, detailData);
                log.debug("Enriched tourist spot detail intro: contentId={}", item.getContentId());
            } else {
                log.warn("No detail intro data found for tourist spot: contentId={}", item.getContentId());
            }

        } catch (Exception e) {
            log.error("Failed to enrich tourist spot detail intro: contentId={}, error={}",
                    item.getContentId(), e.getMessage());
        }
    }

    private void enrichWithDetailCommon(TourismRawData item) {
        try {
            TourismRawData detailCommonData = apiService.fetchDetailCommon(item.getContentId(), serviceKey);

            if (detailCommonData != null) {
                item.setOverview(detailCommonData.getOverview());
                item.setLabelDepth1(detailCommonData.getLabelDepth1());
                item.setLabelDepth2(detailCommonData.getLabelDepth2());
                item.setLabelDepth3(detailCommonData.getLabelDepth3());

                log.debug("Enriched tourist spot detail common: contentId={}", item.getContentId());
            } else {
                log.warn("No detail common data found for tourist spot: contentId={}", item.getContentId());
            }

        } catch (Exception e) {
            log.error("Failed to enrich tourist spot detail common: contentId={}, error={}",
                    item.getContentId(), e.getMessage());
        }
    }

    private boolean isValidTouristSpotData(TourismRawData item) {
        return item.getContentId() != null &&
                !item.getContentId().trim().isEmpty() &&
                item.getTitle() != null &&
                !item.getTitle().trim().isEmpty() &&
                item.getMapX() != null &&
                item.getMapY() != null &&
                CONTENT_TYPE_ID.equals(item.getContentTypeId());
    }
}