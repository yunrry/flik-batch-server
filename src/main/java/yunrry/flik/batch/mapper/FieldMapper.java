package yunrry.flik.batch.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import yunrry.flik.batch.domain.TourismRawData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Component
public class FieldMapper {

    public String getContentTypeName(String contentTypeId) {
        Map<String, String> typeMap = Map.of(
                "12", "관광지",
                "14", "문화시설",
                "15", "축제공연행사",
                "28", "레포츠",
                "32", "숙박",
                "38", "쇼핑",
                "39", "음식점"
        );
        return typeMap.getOrDefault(contentTypeId, "기타");
    }

    public void mapCommonFields(TourismRawData item, Map<String, Object> detailData) {
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

    public void mapDomainSpecificFields(TourismRawData item, Map<String, Object> detailData) {
        Map<String, String> domainFields = new HashMap<>();
        String contentTypeId = item.getContentTypeId();

        List<String> fieldNames = getDomainFieldNames(contentTypeId);
        for (String fieldName : fieldNames) {
            String value = detailData.getOrDefault(fieldName, "").toString();
            domainFields.put(fieldName, value);
        }

        item.setDomainFields(domainFields);
    }

    public List<String> getDomainFieldNames(String contentTypeId) {
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

    public void setFieldValue(TourismRawData item, String fieldName, String value) {
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
