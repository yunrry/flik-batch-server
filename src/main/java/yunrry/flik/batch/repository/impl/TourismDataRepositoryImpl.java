package yunrry.flik.batch.repository.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import yunrry.flik.batch.domain.TourismRawData;
import yunrry.flik.batch.repository.TourismDataRepository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@Slf4j
@Repository
@RequiredArgsConstructor
public class TourismDataRepositoryImpl implements TourismDataRepository {

    private final JdbcTemplate jdbcTemplate;

    @Override
    @Transactional
    public void saveAreaBasedData(TourismRawData data) {
        String tableName = getTableName(data.getContentTypeId());
        String sql = buildInsertSql(tableName);

        jdbcTemplate.update(sql, ps -> setBasicParameters(ps, data));
    }

    @Override
    @Transactional
    public void updateDetailData(TourismRawData data) {
        String tableName = getTableName(data.getContentTypeId());
        String sql = buildUpdateSql(tableName, data.getContentTypeId());

        jdbcTemplate.update(sql, ps -> setDetailParameters(ps, data));
    }

    @Override
    public List<TourismRawData> findUnprocessedForDetail() {
        String sql = """
        (SELECT content_id, content_type_id, '12' as table_type FROM fetched_tourist_attractions WHERE usetime IS NULL OR usetime = '')
        UNION ALL
        (SELECT content_id, content_type_id, '14' as table_type FROM fetched_cultural_facilities WHERE usetime IS NULL OR usetime = '')
        UNION ALL
        (SELECT content_id, content_type_id, '15' as table_type FROM fetched_festivals_events WHERE usetime IS NULL OR usetime = '')
        UNION ALL
        (SELECT content_id, content_type_id, '28' as table_type FROM fetched_sports_recreation WHERE usetime IS NULL OR usetime = '')
        UNION ALL
        (SELECT content_id, content_type_id, '32' as table_type FROM fetched_accommodations WHERE usetime IS NULL OR usetime = '')
        UNION ALL
        (SELECT content_id, content_type_id, '38' as table_type FROM fetched_shopping WHERE usetime IS NULL OR usetime = '')
        UNION ALL
        (SELECT content_id, content_type_id, '39' as table_type FROM fetched_restaurants WHERE usetime IS NULL OR usetime = '')
        LIMIT 10000
        """;

        return jdbcTemplate.query(sql, (rs, rowNum) ->
                TourismRawData.builder()
                        .contentId(rs.getString("content_id"))
                        .contentTypeId(rs.getString("content_type_id"))
                        .build()
        );
    }

    @Override
    public void markAsProcessed(String contentId) {
        // 각 테이블에서 해당 content_id의 updated_at 갱신
        String[] tables = {
                "fetched_tourist_attractions", "fetched_cultural_facilities",
                "fetched_festivals_events", "fetched_sports_recreation",
                "fetched_accommodations", "fetched_shopping", "fetched_restaurants"
        };

        for (String table : tables) {
            jdbcTemplate.update(
                    "UPDATE " + table + " SET updated_at = CURRENT_TIMESTAMP WHERE content_id = ?",
                    contentId
            );
        }
    }

    private String getTableName(String contentTypeId) {
        return switch (contentTypeId) {
            case "12" -> "fetched_tourist_attractions";
            case "14" -> "fetched_cultural_facilities";
            case "15" -> "fetched_festivals_events";
            case "28" -> "fetched_sports_recreation";
            case "32" -> "fetched_accommodations";  // 추가
            case "38" -> "fetched_shopping";
            case "39" -> "fetched_restaurants";
            default -> throw new IllegalArgumentException("Unknown content type: " + contentTypeId);
        };
    }

    private String buildInsertSql(String tableName) {
        return String.format("""
            INSERT INTO %s (
                content_id, content_type_id, content_type_name, title, addr1, addr2,
                first_image, first_image2, map_x, map_y, area_code, sigungu_code,
                cat1, cat2, cat3, created_time, modified_time, tel, zipcode, overview, source
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE
                title = VALUES(title),
                addr1 = VALUES(addr1),
                first_image = VALUES(first_image),
                modified_time = VALUES(modified_time),
                updated_at = CURRENT_TIMESTAMP
            """, tableName);
    }

    private String buildUpdateSql(String tableName, String contentTypeId) {
        StringBuilder sql = new StringBuilder(String.format(
                "UPDATE %s SET usetime = ?, restdate = ?, parking = ?, parkingfee = ?, " +
                        "infocenter = ?, chkbabycarriage = ?, chkpet = ?, chkcreditcard = ?", tableName));

        // 도메인별 특화 필드 추가
        List<String> domainFields = getDomainFields(contentTypeId);
        for (String field : domainFields) {
            sql.append(", ").append(field).append(" = ?");
        }

        sql.append(", updated_at = CURRENT_TIMESTAMP WHERE content_id = ?");
        return sql.toString();
    }

    private List<String> getDomainFields(String contentTypeId) {
        return switch (contentTypeId) {
            case "12" -> List.of("heritage1", "heritage2", "heritage3", "opendate",
                    "expguide", "expagerange", "accomcount", "useseason");
            case "14" -> List.of("scale", "usefee", "discountinfo", "spendtime");
            case "15" -> List.of("sponsor1", "sponsor1tel", "sponsor2", "sponsor2tel",
                    "eventenddate", "playtime", "eventplace", "eventhomepage",
                    "agelimit", "bookingplace", "placeinfo", "subevent", "program",
                    "eventstartdate", "usetimefestival", "discountinfofestival",
                    "spendtimefestival", "festivalgrade", "progresstype", "festivaltype");
            case "28" -> List.of("openperiod", "reservation", "scaleleports",
                    "accomcountleports", "usefeeleports", "expagerangeleports");
            case "32" -> List.of("roomcount", "roomtype", "refundregulation", "checkintime", "checkouttime",
                    "chkcooking", "seminar", "sports", "sauna", "beauty", "beverage", "karaoke",
                    "barbecue", "campfire", "bicycle", "fitness", "publicpc", "publicbath",
                    "subfacility", "foodplace", "reservationurl", "pickup", "reservationlodging",
                    "scalelodging", "accomcountlodging");
            case "38" -> List.of("saleitem", "saleitemcost", "fairday", "opendateshopping",
                    "shopguide", "culturecenter", "restroom", "scaleshopping");
            case "39" -> List.of("seat", "kidsfacility", "firstmenu", "treatmenu", "smoking",
                    "packing", "scalefood", "opendatefood", "discountinfofood",
                    "reservationfood", "lcnsno");
            default -> List.of();
        };
    }

    private void setBasicParameters(PreparedStatement ps, TourismRawData data) throws SQLException {
        ps.setString(1, data.getContentId());
        ps.setString(2, data.getContentTypeId());
        ps.setString(3, data.getContentTypeName());
        ps.setString(4, data.getTitle());
        ps.setString(5, data.getAddr1());
        ps.setString(6, data.getAddr2());
        ps.setString(7, data.getFirstImage());
        ps.setString(8, data.getFirstImage2());
        ps.setString(9, data.getMapX());
        ps.setString(10, data.getMapY());
        ps.setString(11, data.getAreaCode());
        ps.setString(12, data.getSigunguCode());
        ps.setString(13, data.getCat1());
        ps.setString(14, data.getCat2());
        ps.setString(15, data.getCat3());
        ps.setString(16, data.getCreatedTime());
        ps.setString(17, data.getModifiedTime());
        ps.setString(18, data.getTel());
        ps.setString(19, data.getZipcode());
        ps.setString(20, data.getOverview());
        ps.setString(21, data.getSource());
    }


    private void setDetailParameters(PreparedStatement ps, TourismRawData data) throws SQLException {
        int paramIndex = 1;

        // 공통 필드
        ps.setString(paramIndex++, data.getUsetime());
        ps.setString(paramIndex++, data.getRestdate());
        ps.setString(paramIndex++, data.getParking());
        ps.setString(paramIndex++, data.getParkingfee());
        ps.setString(paramIndex++, data.getInfocenter());
        ps.setString(paramIndex++, data.getChkbabycarriage());
        ps.setString(paramIndex++, data.getChkpet());
        ps.setString(paramIndex++, data.getChkcreditcard());

        // 도메인별 특화 필드
        List<String> domainFields = getDomainFields(data.getContentTypeId());
        Map<String, String> domainFieldsMap = data.getDomainFields();

        for (String field : domainFields) {
            String value = domainFieldsMap != null ? domainFieldsMap.get(field) : "";
            ps.setString(paramIndex++, value != null ? value : "");
        }

        // WHERE 조건
        ps.setString(paramIndex, data.getContentId());
    }
}