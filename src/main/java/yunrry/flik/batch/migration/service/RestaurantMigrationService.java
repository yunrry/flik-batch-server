package yunrry.flik.batch.migration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yunrry.flik.batch.migration.enums.MainCategory;
import yunrry.flik.batch.migration.enums.RegionCode;
import yunrry.flik.batch.migration.enums.SigunguCode;
import yunrry.flik.batch.migration.enums.SubCategory;
import yunrry.flik.batch.migration.mapper.CategoryMapper;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RestaurantMigrationService {

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;
    private final CategoryMapper categoryMapper;

    public boolean migrateRestaurants() {
        log.info("음식점 데이터 마이그레이션 시작");

        try {
            List<Map<String, Object>> restaurantData = getRestaurantsData();
            if (restaurantData.isEmpty()) {
                log.warn("마이그레이션할 데이터가 없습니다.");
                return false;
            }

            List<Map<String, Object>> spotsData = transformToSpotsData(restaurantData);
            log.info("데이터 변환 완료: {}건", spotsData.size());

            boolean success = insertIntoSpots(spotsData);

            if (success) {
                log.info("✅ 음식점 데이터 마이그레이션 완료!");
            } else {
                log.error("❌ 음식점 데이터 마이그레이션 실패");
            }

            return success;

        } catch (Exception e) {
            log.error("마이그레이션 중 오류 발생", e);
            return false;
        }
    }

    private List<Map<String, Object>> getRestaurantsData() {
        String query = """
            SELECT * FROM fetched_restaurants
            WHERE label_depth1 IS NOT NULL 
            AND label_depth1 != '' 
            AND addr1 IS NOT NULL 
            AND addr1 != '' 
            AND overview IS NOT NULL
            AND overview != ''
            ORDER BY id
            """;

        List<Map<String, Object>> data = jdbcTemplate.queryForList(query);
        log.info("fetched_restaurants에서 {}건 조회", data.size());
        return data;
    }

    private List<Map<String, Object>> transformToSpotsData(List<Map<String, Object>> restaurantData) {
        return restaurantData.stream().map(item -> {
            String regnCd = RegionCode.getRegnCd(getString(item, "area_code"));
            String signguCd = SigunguCode.parseAddressToCode(getString(item, "addr1"));

            List<String> imageUrls = new ArrayList<>();
            String firstImage = getString(item, "first_image");
            String firstImage2 = getString(item, "first_image2");
            if (!firstImage.isEmpty()) imageUrls.add(firstImage);
            if (!firstImage2.isEmpty()) imageUrls.add(firstImage2);

            Map<String, Object> spotData = new HashMap<>();
            spotData.put("spot_type", "RESTAURANT");
            spotData.put("address", getString(item, "addr1"));
            spotData.put("baby_carriage", getString(item, "chkbabycarriage"));
            spotData.put("category", getString(item, "content_type_name"));
            spotData.put("close_time", null);
            String labelDepth2 = getString(item, "label_depth2");

            SubCategory subCategory = SubCategory.findByKoreanName(labelDepth2);
            MainCategory mainCategory = subCategory != null ?
                    categoryMapper.getMainCategory(subCategory) : null;
//            String categoryValue = mainCategory != null ?
//                    mainCategory.getCode() : "other";
            spotData.put("category", mainCategory);

            spotData.put("content_id", getString(item, "content_id"));
            spotData.put("day_off", getString(item, "restdate"));
            spotData.put("description", getString(item, "overview"));
            spotData.put("google_place_id", getString(item, "google_place_id"));

            try {
                spotData.put("image_urls", imageUrls.isEmpty() ? null :
                        objectMapper.writeValueAsString(imageUrls));
            } catch (Exception e) {
                spotData.put("image_urls", null);
            }

            spotData.put("info", getString(item, "infocenter"));
            spotData.put("latitude", getDouble(item, "map_y"));
            spotData.put("longitude", getDouble(item, "map_x"));
            spotData.put("name", getString(item, "title"));
            spotData.put("open_time", null);
            spotData.put("parking", getString(item, "parking"));
            spotData.put("pet_carriage", getString(item, "chkpet"));
            spotData.put("rating", getBigDecimal(item, "google_rating"));
            spotData.put("regn_cd", regnCd);
            spotData.put("review_count", getInteger(item, "google_review_count"));
            spotData.put("signgu_cd", signguCd);
            spotData.put("tag1", null);
            spotData.put("tag2", null);
            spotData.put("tag3", null);
            spotData.put("tags", null);
            spotData.put("label_depth1", getString(item, "label_depth1"));
            spotData.put("label_depth2", getString(item, "label_depth2"));
            spotData.put("label_depth3", getString(item, "label_depth3"));
            spotData.put("check_in_time", "");
            spotData.put("check_out_time", "");
            spotData.put("cooking", null);
            spotData.put("facilities", "");
            spotData.put("cuisine_type", getString(item, "cat3"));
            spotData.put("fee", "");
            spotData.put("age_limit", "");
            spotData.put("event_end_date", "");
            spotData.put("event_start_date", "");
            spotData.put("running_time", "");
            spotData.put("sponsor", "");
            spotData.put("first_menu", getString(item, "firstmenu"));
            spotData.put("kids_facility", getString(item, "kidsfacility"));
            spotData.put("price_range", "");
            spotData.put("reservation", getString(item, "reservationfood"));
            spotData.put("take_away", getString(item, "packing"));
            spotData.put("treat_menu", getString(item, "treatmenu"));
            spotData.put("products", "");
            spotData.put("exp_guide", getString(item, "scalefood"));
            spotData.put("time", getString(item, "usetime"));

            return spotData;
        }).collect(Collectors.toList());
    }

    @Transactional
    public boolean insertIntoSpots(List<Map<String, Object>> spotsData) {
        if (spotsData.isEmpty()) {
            return true;
        }

        try {
            String insertQuery = """
                INSERT IGNORE INTO spots (
                    spot_type, address, baby_carriage, category, close_time, content_type_id, content_id,
                    day_off, description, google_place_id, image_urls, info, latitude,
                    longitude, name, open_time, parking, pet_carriage, rating, regn_cd,
                    review_count, signgu_cd, tag1, tag2, tag3, tags, label_depth1, label_depth2, label_depth3, check_in_time,
                    check_out_time, cooking, facilities, cuisine_type, fee, age_limit,
                    event_end_date, event_start_date, running_time, sponsor, first_menu,
                    kids_facility, price_range, reservation, take_away, treat_menu,
                    products, exp_guide, time
                ) VALUES (
                    ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?
                )
                """;

            List<Object[]> batchArgs = spotsData.stream().map(item -> new Object[]{
                    item.get("spot_type"),
                    item.get("address"),
                    item.get("baby_carriage"),
                    item.get("category"),
                    item.get("close_time"),
                    item.get("content_type_id"),
                    item.get("content_id"),
                    item.get("day_off"),
                    item.get("description"),
                    item.get("google_place_id"),
                    item.get("image_urls"),
                    item.get("info"),
                    item.get("latitude"),
                    item.get("longitude"),
                    item.get("name"),
                    item.get("open_time"),
                    item.get("parking"),
                    item.get("pet_carriage"),
                    item.get("rating"),
                    item.get("regn_cd"),
                    item.get("review_count"),
                    item.get("signgu_cd"),
                    item.get("tag1"),
                    item.get("tag2"),
                    item.get("tag3"),
                    item.get("tags"),
                    item.get("label_depth1"),
                    item.get("label_depth2"),
                    item.get("label_depth3"),
                    item.get("check_in_time"),
                    item.get("check_out_time"),
                    item.get("cooking"),
                    item.get("facilities"),
                    item.get("cuisine_type"),
                    item.get("fee"),
                    item.get("age_limit"),
                    item.get("event_end_date"),
                    item.get("event_start_date"),
                    item.get("running_time"),
                    item.get("sponsor"),
                    item.get("first_menu"),
                    item.get("kids_facility"),
                    item.get("price_range"),
                    item.get("reservation"),
                    item.get("take_away"),
                    item.get("treat_menu"),
                    item.get("products"),
                    item.get("exp_guide"),
                    item.get("time")
            }).collect(Collectors.toList());

            jdbcTemplate.batchUpdate(insertQuery, batchArgs);
            log.info("spots 테이블에 {}건 삽입 완료", spotsData.size());
            return true;

        } catch (Exception e) {
            log.error("spots 테이블 삽입 실패", e);
            return false;
        }
    }

    private String getString(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : "";
    }

    private Double getDouble(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;
        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Integer getInteger(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private BigDecimal getBigDecimal(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;
        try {
            return new BigDecimal(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}