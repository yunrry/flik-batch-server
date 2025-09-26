package yunrry.flik.batch.job.processor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;
import yunrry.flik.batch.domain.TourismRawData;
import yunrry.flik.batch.exception.ApiLimitExceededException;
import yunrry.flik.batch.mapper.FieldMapper;
import yunrry.flik.batch.service.ApiService;
import org.springframework.batch.core.StepExecution;
import yunrry.flik.batch.service.NotificationService;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class DynamicTourismProcessor implements ItemProcessor<TourismRawData, TourismRawData> {

    private final NotificationService notificationService;
    private final ApiService apiService;
    private final FieldMapper fieldMapper;

    private String serviceKey;
    private String contentTypeId;

    @BeforeStep
    public void beforeStep(StepExecution stepExecution) {
        JobParameters jobParameters = stepExecution.getJobParameters();
        this.serviceKey = jobParameters.getString("serviceKey");
        this.contentTypeId = jobParameters.getString("contentTypeId");
    }

    @Override
    public TourismRawData process(TourismRawData item) throws Exception {
        if (!contentTypeId.equals(item.getContentTypeId())) {
            return null;
        }

        if (!isValidData(item)) {
            return null;
        }

        item.setContentTypeName(fieldMapper.getContentTypeName(contentTypeId));
        item.setCollectedAt(LocalDateTime.now());
        item.setSource("http://apis.data.go.kr/B551011/KorService2");

        try {
            enrichWithDetailIntro(item);
        } catch (ApiLimitExceededException e) {
            log.warn("DetailIntro API limit exceeded, skipping for: {}", item.getContentId());
            notificationService.sendAlert("DetailIntro API 한도 초과");
        }

        try {
            enrichWithDetailCommon(item);
        } catch (ApiLimitExceededException e) {
            log.warn("DetailCommon API limit exceeded, skipping for: {}", item.getContentId());
            notificationService.sendAlert("DetailCommon API 한도 초과");
        }

        return item;
    }

    private void enrichWithDetailIntro(TourismRawData item) {
        try {
            Map<String, Object> detailData = apiService.fetchDetailIntro(
                    item.getContentId(), contentTypeId, serviceKey);

            if (detailData != null && !detailData.isEmpty()) {
                fieldMapper.mapCommonFields(item, detailData);
                fieldMapper.mapDomainSpecificFields(item, detailData);
            }
        } catch (Exception e) {
            log.error("Failed to enrich detail intro: {}", item.getContentId(), e);
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
            }
        } catch (Exception e) {
            log.error("Failed to enrich detail common: {}", item.getContentId(), e);
        }
    }

    private boolean isValidData(TourismRawData item) {
        return item.getContentId() != null && !item.getContentId().trim().isEmpty() &&
                item.getTitle() != null && !item.getTitle().trim().isEmpty();
    }
}
