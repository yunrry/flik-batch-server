package yunrry.flik.batch.repository;

import yunrry.flik.batch.domain.TourismRawData;
import java.util.List;

public interface TourismDataRepository {
    void saveAreaBasedData(TourismRawData data);
    void updateDetailData(TourismRawData data);
    List<TourismRawData> findUnprocessedForDetail();
    void markAsProcessed(String contentId);
}