package yunrry.flik.batch.job.processor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;
import yunrry.flik.batch.domain.SpotImageRecord;
import yunrry.flik.batch.domain.SpotImageUpdate;
import yunrry.flik.batch.service.SpotImageApiClient;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class SpotImageUpdateProcessor implements ItemProcessor<SpotImageRecord, SpotImageUpdate> {

    private final SpotImageApiClient apiClient;

    @Override
    public SpotImageUpdate process(SpotImageRecord item) {
        if (item.getContentId() == null || item.getContentId().isBlank()) return null;

        List<String> urls = apiClient.fetchOriginImageUrls(item.getContentId());
        if (urls.isEmpty()) {
            // 업데이트할 값이 없으면 스킵
            return null;
        }
        String joined = urls.stream().distinct().collect(Collectors.joining(","));
        return new SpotImageUpdate(item.getId(), joined);
    }
}