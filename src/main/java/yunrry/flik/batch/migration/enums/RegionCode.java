package yunrry.flik.batch.migration.enums;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum RegionCode {
    SEOUL("1", "11", "서울시"),
    INCHEON("2", "28", "인천시"),
    DAEJEON("3", "30", "대전시"),
    DAEGU("4", "27", "대구시"),
    GWANGJU("5", "29", "광주시"),
    BUSAN("6", "26", "부산시"),
    ULSAN("7", "31", "울산시"),
    GYEONGGI("9", "36", "경기도"),
    GANGWON("10", "41", "강원도"),
    CHUNGBUK("11", "43", "충청북도"),
    CHUNGNAM("12", "44", "충청남도"),
    GYEONGBUK("13", "48", "경상북도"),
    GYEONGNAM("14", "50", "경상남도"),
    JEONBUK("15", "46", "전라북도"),
    JEONNAM("16", "47", "전라남도"),
    JEJU("17", "51", "제주도");

    private final String areaCode;
    private final String regnCd;
    private final String name;

    private static final Map<String, RegionCode> AREA_CODE_MAP =
            Arrays.stream(values()).collect(Collectors.toMap(RegionCode::getAreaCode, Function.identity()));

    RegionCode(String areaCode, String regnCd, String name) {
        this.areaCode = areaCode;
        this.regnCd = regnCd;
        this.name = name;
    }

    public static String getRegnCd(String areaCode) {
        RegionCode region = AREA_CODE_MAP.get(areaCode);
        return region != null ? region.regnCd : "00";
    }

    public String getAreaCode() { return areaCode; }
    public String getRegnCd() { return regnCd; }
    public String getName() { return name; }
}