package yunrry.flik.batch.repository.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import yunrry.flik.batch.domain.SpotImageRecord;
import yunrry.flik.batch.domain.SpotImageUpdate;
import yunrry.flik.batch.repository.SpotImageRepository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class SpotImageRepositoryImpl implements SpotImageRepository {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<SpotImageRecord> findNextWithoutImages(long lastIdExclusive, int limit) {
        String sql = """
            SELECT id, content_id
            FROM spots
            WHERE image_urls IS NULL
              AND content_id IS NOT NULL
              AND id > ?
            ORDER BY id ASC
            LIMIT ?
            """;
        return jdbcTemplate.query(sql,
                (rs, i) -> new SpotImageRecord(rs.getLong("id"), rs.getString("content_id")),
                lastIdExclusive, limit);
    }

    @Override
    public void updateImageUrls(List<? extends SpotImageUpdate> items) {
        if (items == null || items.isEmpty()) return;
        String sql = "UPDATE spots SET image_urls = ? WHERE id = ?";
        List<Object[]> args = items.stream()
                .map(u -> new Object[]{u.getImageUrls(), u.getId()})
                .toList();
        jdbcTemplate.batchUpdate(sql, args);
    }
}