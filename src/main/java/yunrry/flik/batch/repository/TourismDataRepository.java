package yunrry.flik.batch.repository;

import yunrry.flik.batch.domain.TourismRawData;
import java.util.List;
import java.util.Map;


public interface TourismDataRepository {
    void saveAreaBasedData(TourismRawData data);
    void updateDetailData(TourismRawData data);
    List<TourismRawData> findUnprocessedForDetail();
    List<TourismRawData> findUnprocessedForLabelDetail();
    void updateLabelDetailData(TourismRawData data);
    Map<String, String> findLabelNames(String code1, String code2, String code3);
    void markAsProcessed(String contentId);
}