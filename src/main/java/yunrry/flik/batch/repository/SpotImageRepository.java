package yunrry.flik.batch.repository;

import yunrry.flik.batch.domain.SpotImageRecord;
import yunrry.flik.batch.domain.SpotImageUpdate;

import java.util.List;

public interface SpotImageRepository {
    List<SpotImageRecord> findNextWithoutImages(long lastIdExclusive, int limit);
    void updateImageUrls(List<? extends SpotImageUpdate> items);
}