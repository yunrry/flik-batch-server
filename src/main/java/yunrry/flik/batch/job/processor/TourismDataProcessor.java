package yunrry.flik.batch.job.processor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;
import yunrry.flik.batch.domain.TourismRawData;
import yunrry.flik.batch.service.ApiService;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class TourismDataProcessor implements ItemProcessor<TourismRawData, TourismRawData> {

    private final ApiService apiService;

    @Override
    public TourismRawData process(TourismRawData item) throws Exception {
        try {
            // 기본 데이터 검증
            if (!isValidData(item)) {
                log.warn("Invalid data skipped: contentId={}, title={}", item.getContentId(), item.getTitle());
                return null;
            }

            // 컨텐츠 타입별 매핑
            item.setContentTypeName(getContentTypeName(item.getContentTypeId()));

            // 수집 시간 설정
            item.setCollectedAt(LocalDateTime.now());

            // 소스 정보 설정
            item.setSource("http://apis.data.go.kr/B551011/KorService2");

            log.debug("Processed item: contentId={}, type={}", item.getContentId(), item.getContentTypeName());
            return item;
        } catch (Exception e) {
            log.error("Processing failed: contentId={}, error={}", item.getContentId(), e.getMessage());
            return null;
        }
    }

    public ItemProcessor<TourismRawData, TourismRawData> createDetailProcessor() {
        return new ItemProcessor<TourismRawData, TourismRawData>() {
            @Override
            public TourismRawData process(TourismRawData item) throws Exception {
                try {
                    // detailIntro2 API 호출
                    Map<String, Object> detailData = apiService.fetchDetailIntro(
                            item.getContentId(),
                            item.getContentTypeId()
                    );

                    // 공통 컬럼 매핑
                    mapCommonFields(item, detailData);

                    // 도메인별 특화 필드 매핑
                    mapDomainSpecificFields(item, detailData);

                    return item;
                } catch (Exception e) {
                    log.error("Error processing detail for contentId: {}", item.getContentId(), e);
                    return item; // 원본 데이터 유지
                }
            }
        };
    }

    private boolean isValidData(TourismRawData item) {
        return item.getContentId() != null &&
                item.getContentTypeId() != null &&
                item.getTitle() != null;
    }

    private String getContentTypeName(String contentTypeId) {
        Map<String, String> typeMap = Map.of(
                "12", "관광지",
                "14", "문화시설",
                "15", "축제공연행사",
                "25", "여행코스",
                "28", "레포츠",
                "32", "숙박",
                "38", "쇼핑",
                "39", "음식점"
        );
        return typeMap.getOrDefault(contentTypeId, "기타");
    }

    private void mapCommonFields(TourismRawData item, Map<String, Object> detailData) {
        // 공통 컬럼 매핑 로직 (Python 코드 참조)
        Map<String, List<String>> commonMappings = Map.of(
                "usetime", List.of("usetime", "usetimeculture", "usetimeleports", "opentime", "opentimefood"),
                "restdate", List.of("restdate", "restdateculture", "restdateleports", "restdateshopping", "restdatefood"),
                "parking", List.of("parking", "parkingculture", "parkingleports", "parkinglodging", "parkingshopping", "parkingfood"),
                "parkingfee", List.of("parkingfeeleports"),
                "infocenter", List.of("infocenter", "infocenterculture", "infocenterleports", "infocenterlodging", "infocentershopping", "infocenterfood"),
                "chkbabycarriage", List.of("chkbabycarriage", "chkbabycarriageculture", "chkbabycarriageleports", "chkbabycarriageshopping"),
                "chkpet", List.of("chkpet", "chkpetculture", "chkpetshopping"),
                "chkcreditcard", List.of("chkcreditcard", "chkcreditcardculture", "chkcreditcardleports", "chkcreditcardshopping", "chkcreditcardfood")
        );

        commonMappings.forEach((commonKey, apiKeys) -> {
            String value = "";
            for (String apiKey : apiKeys) {
                if (detailData.containsKey(apiKey) && detailData.get(apiKey) != null) {
                    value = detailData.get(apiKey).toString();
                    break;
                }
            }
            setFieldValue(item, commonKey, value);
        });
    }

    private void mapDomainSpecificFields(TourismRawData item, Map<String, Object> detailData) {
        Map<String, String> domainFields = new HashMap<>();
        String contentTypeId = item.getContentTypeId();

        List<String> fieldNames = getDomainFieldNames(contentTypeId);
        for (String fieldName : fieldNames) {
            String value = detailData.getOrDefault(fieldName, "").toString();
            domainFields.put(fieldName, value);
        }

        item.setDomainFields(domainFields);
    }

    private List<String> getDomainFieldNames(String contentTypeId) {
        return switch (contentTypeId) {
            case "12" -> List.of("heritage1", "heritage2", "heritage3", "opendate", "expguide",
                    "expagerange", "accomcount", "useseason");
            case "14" -> List.of("scale", "usefee", "discountinfo", "spendtime");
            case "15" -> List.of("sponsor1", "sponsor1tel", "sponsor2", "sponsor2tel", "eventenddate",
                    "playtime", "eventplace", "eventhomepage", "agelimit", "bookingplace",
                    "placeinfo", "subevent", "program", "eventstartdate", "usetimefestival",
                    "discountinfofestival", "spendtimefestival", "festivalgrade",
                    "progresstype", "festivaltype");
            case "28" -> List.of("openperiod", "reservation", "scaleleports", "accomcountleports",
                    "usefeeleports", "expagerangeleports");
            case "38" -> List.of("saleitem", "saleitemcost", "fairday", "opendateshopping", "shopguide",
                    "culturecenter", "restroom", "scaleshopping");
            case "39" -> List.of("seat", "kidsfacility", "firstmenu", "treatmenu", "smoking", "packing",
                    "scalefood", "opendatefood", "discountinfofood", "reservationfood", "lcnsno");
            default -> List.of();
        };
    }

    private void setFieldValue(TourismRawData item, String fieldName, String value) {
        switch (fieldName) {
            case "usetime" -> item.setUsetime(value);
            case "restdate" -> item.setRestdate(value);
            case "parking" -> item.setParking(value);
            case "parkingfee" -> item.setParkingfee(value);
            case "infocenter" -> item.setInfocenter(value);
            case "chkbabycarriage" -> item.setChkbabycarriage(value);
            case "chkpet" -> item.setChkpet(value);
            case "chkcreditcard" -> item.setChkcreditcard(value);
        }
    }
}