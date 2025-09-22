package yunrry.flik.batch.migration.enums;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

// 시군구 코드 매핑 Enum
public enum SigunguCode {
    // 서울시
    SEOUL_JONGNO("종로구", "110"),
    SEOUL_JUNG("중구", "140"),
    SEOUL_YONGSAN("용산구", "170"),
    SEOUL_SEONGDONG("성동구", "200"),
    SEOUL_GWANGJIN("광진구", "215"),
    SEOUL_DONGDAEMUN("동대문구", "230"),
    SEOUL_JUNGNANG("중랑구", "260"),
    SEOUL_SEONGBUK("성북구", "290"),
    SEOUL_GANGBUK("강북구", "305"),
    SEOUL_DOBONG("도봉구", "320"),
    SEOUL_NOWON("노원구", "350"),
    SEOUL_EUNPYEONG("은평구", "380"),
    SEOUL_SEODAEMUN("서대문구", "410"),
    SEOUL_MAPO("마포구", "440"),
    SEOUL_YANGCHEON("양천구", "470"),
    SEOUL_GANGSEO("강서구", "500"),
    SEOUL_GURO("구로구", "530"),
    SEOUL_GEUMCHEON("금천구", "545"),
    SEOUL_YEONGDEUNGPO("영등포구", "560"),
    SEOUL_DONGJAK("동작구", "590"),
    SEOUL_GWANAK("관악구", "620"),
    SEOUL_SEOCHO("서초구", "650"),
    SEOUL_GANGNAM("강남구", "680"),
    SEOUL_SONGPA("송파구", "710"),
    SEOUL_GANGDONG("강동구", "740"),

    // 부산시
    BUSAN_JUNG("중구", "110"),
    BUSAN_SEO("서구", "140"),
    BUSAN_DONG("동구", "170"),
    BUSAN_YEONGDO("영도구", "200"),
    BUSAN_BUSANJIN("부산진구", "230"),
    BUSAN_DONGNAE("동래구", "260"),
    BUSAN_NAM("남구", "290"),
    BUSAN_BUK("북구", "320"),
    BUSAN_HAEUNDAE("해운대구", "350"),
    BUSAN_SAHA("사하구", "380"),
    BUSAN_GEUMJEONG("금정구", "410"),
    BUSAN_GANGSEO_BUSAN("강서구", "440"),
    BUSAN_YEONJE("연제구", "470"),
    BUSAN_SUYEONG("수영구", "500"),
    BUSAN_SASANG("사상구", "530"),
    BUSAN_GIJANG("기장군", "710"),

    // 대구시
    DAEGU_JUNG("중구", "110"),
    DAEGU_DONG("동구", "140"),
    DAEGU_SEO("서구", "170"),
    DAEGU_NAM("남구", "200"),
    DAEGU_BUK("북구", "230"),
    DAEGU_SUSEONG("수성구", "260"),
    DAEGU_DALSEO("달서구", "290"),
    DAEGU_DALSEONG("달성군", "710"),
    DAEGU_GUNWI("군위군", "720"),

    // 인천시
    INCHEON_JUNG("중구", "110"),
    INCHEON_DONG("동구", "140"),
    INCHEON_MICHUHOL("미추홀구", "177"),
    INCHEON_YEONSU("연수구", "185"),
    INCHEON_NAMDONG("남동구", "200"),
    INCHEON_BUPYEONG("부평구", "237"),
    INCHEON_GYEYANG("계양구", "245"),
    INCHEON_SEO("서구", "260"),
    INCHEON_GANGHWA("강화군", "710"),
    INCHEON_ONGJIN("옹진군", "720"),

    // 광주시
    GWANGJU_DONG("동구", "110"),
    GWANGJU_SEO("서구", "140"),
    GWANGJU_NAM("남구", "155"),
    GWANGJU_BUK("북구", "170"),
    GWANGJU_GWANGSAN("광산구", "200"),

    // 대전시
    DAEJEON_DONG("동구", "110"),
    DAEJEON_JUNG("중구", "140"),
    DAEJEON_SEO("서구", "170"),
    DAEJEON_YUSEONG("유성구", "200"),
    DAEJEON_DAEDEOK("대덕구", "230"),

    // 울산시
    ULSAN_JUNG("중구", "110"),
    ULSAN_NAM("남구", "140"),
    ULSAN_DONG("동구", "170"),
    ULSAN_BUK("북구", "200"),
    ULSAN_ULJU("울주군", "710"),

    // 세종시
    SEJONG_CITY("세종특별자치시", "110"),

    // 경기도
    GYEONGGI_SUWON("수원시", "110"),
    GYEONGGI_SEONGNAM("성남시", "130"),
    GYEONGGI_UIJEONGBU("의정부시", "150"),
    GYEONGGI_ANYANG("안양시", "170"),
    GYEONGGI_BUCHEON("부천시", "190"),
    GYEONGGI_GWANGMYEONG("광명시", "210"),
    GYEONGGI_PYEONGTAEK("평택시", "220"),
    GYEONGGI_DONGDUCHEON("동두천시", "250"),
    GYEONGGI_ANSAN("안산시", "270"),
    GYEONGGI_GOYANG("고양시", "280"),
    GYEONGGI_GWACHEON("과천시", "290"),
    GYEONGGI_GURI("구리시", "310"),
    GYEONGGI_NAMYANGJU("남양주시", "360"),
    GYEONGGI_OSAN("오산시", "370"),
    GYEONGGI_SIHEUNG("시흥시", "390"),
    GYEONGGI_GUNPO("군포시", "410"),
    GYEONGGI_UIWANG("의왕시", "430"),
    GYEONGGI_HANAM("하남시", "450"),
    GYEONGGI_YONGIN("용인시", "460"),
    GYEONGGI_PAJU("파주시", "480"),
    GYEONGGI_ICHEON("이천시", "500"),
    GYEONGGI_ANSEONG("안성시", "550"),
    GYEONGGI_GIMPO("김포시", "570"),
    GYEONGGI_HWASEONG("화성시", "590"),
    GYEONGGI_GWANGJU("광주시", "610"),
    GYEONGGI_YANGJU("양주시", "630"),
    GYEONGGI_POCHEON("포천시", "650"),
    GYEONGGI_YEOJU("여주시", "670"),
    GYEONGGI_YEONCHEON("연천군", "800"),
    GYEONGGI_GAPYEONG("가평군", "820"),
    GYEONGGI_YANGPYEONG("양평군", "830"),

    // 강원도
    GANGWON_CHUNCHEON("춘천시", "110"),
    GANGWON_WONJU("원주시", "130"),
    GANGWON_GANGNEUNG("강릉시", "150"),
    GANGWON_DONGHAE("동해시", "170"),
    GANGWON_TAEBAEK("태백시", "190"),
    GANGWON_SOKCHO("속초시", "210"),
    GANGWON_SAMCHEOK("삼척시", "230"),
    GANGWON_HONGCHEON("홍천군", "720"),
    GANGWON_HOENGSEONG("횡성군", "730"),
    GANGWON_YEONGWOL("영월군", "750"),
    GANGWON_PYEONGCHANG("평창군", "760"),
    GANGWON_JEONGSEON("정선군", "770"),
    GANGWON_CHEORWON("철원군", "780"),
    GANGWON_HWACHEON("화천군", "790"),
    GANGWON_YANGGU("양구군", "800"),
    GANGWON_INJE("인제군", "810"),
    GANGWON_GOSEONG("고성군", "820"),
    GANGWON_YANGYANG("양양군", "830"),

    // 충청북도
    CHUNGBUK_CHEONGJU("청주시", "110"),
    CHUNGBUK_CHUNGJU("충주시", "130"),
    CHUNGBUK_JECHEON("제천시", "150"),
    CHUNGBUK_BOEUN("보은군", "720"),
    CHUNGBUK_OKCHEON("옥천군", "730"),
    CHUNGBUK_YEONGDONG("영동군", "740"),
    CHUNGBUK_JEUNGPYEONG("증평군", "745"),
    CHUNGBUK_JINCHEON("진천군", "750"),
    CHUNGBUK_GOESAN("괴산군", "760"),
    CHUNGBUK_EUMSEONG("음성군", "770"),
    CHUNGBUK_DANYANG("단양군", "800"),

    // 충청남도
    CHUNGNAM_CHEONAN("천안시", "110"),
    CHUNGNAM_GONGJU("공주시", "150"),
    CHUNGNAM_BORYEONG("보령시", "180"),
    CHUNGNAM_ASAN("아산시", "200"),
    CHUNGNAM_SEOSAN("서산시", "210"),
    CHUNGNAM_NONSAN("논산시", "230"),
    CHUNGNAM_GYERYONG("계룡시", "250"),
    CHUNGNAM_DANGJIN("당진시", "270"),
    CHUNGNAM_GEUMSAN("금산군", "710"),
    CHUNGNAM_BUYEO("부여군", "760"),
    CHUNGNAM_SEOCHEON("서천군", "770"),
    CHUNGNAM_CHEONGYANG("청양군", "790"),
    CHUNGNAM_HONGSEONG("홍성군", "800"),
    CHUNGNAM_YESAN("예산군", "810"),
    CHUNGNAM_TAEAN("태안군", "825"),

    // 전라북도
    JEONBUK_JEONJU("전주시", "110"),
    JEONBUK_GUNSAN("군산시", "130"),
    JEONBUK_IKSAN("익산시", "140"),
    JEONBUK_JEONGEUP("정읍시", "180"),
    JEONBUK_NAMWON("남원시", "190"),
    JEONBUK_GIMJE("김제시", "210"),
    JEONBUK_WANJU("완주군", "710"),
    JEONBUK_JINAN("진안군", "720"),
    JEONBUK_MUJU("무주군", "730"),
    JEONBUK_JANGSU("장수군", "740"),
    JEONBUK_IMSIL("임실군", "750"),
    JEONBUK_SUNCHANG("순창군", "770"),
    JEONBUK_GOCHANG("고창군", "790"),
    JEONBUK_BUAN("부안군", "800"),

    // 전라남도
    JEONNAM_MOKPO("목포시", "110"),
    JEONNAM_YEOSU("여수시", "130"),
    JEONNAM_SUNCHEON("순천시", "150"),
    JEONNAM_NAJU("나주시", "170"),
    JEONNAM_GWANGYANG("광양시", "230"),
    JEONNAM_DAMYANG("담양군", "710"),
    JEONNAM_GOKSEONG("곡성군", "720"),
    JEONNAM_GURYE("구례군", "730"),
    JEONNAM_GOHEUNG("고흥군", "770"),
    JEONNAM_BOSEONG("보성군", "780"),
    JEONNAM_HWASUN("화순군", "790"),
    JEONNAM_JANGHEUNG("장흥군", "800"),
    JEONNAM_GANGJIN("강진군", "810"),
    JEONNAM_HAENAM("해남군", "820"),
    JEONNAM_YEONGAM("영암군", "830"),
    JEONNAM_MUAN("무안군", "840"),
    JEONNAM_HAMPYEONG("함평군", "860"),
    JEONNAM_YEONGGWANG("영광군", "870"),
    JEONNAM_JANGSEONG("장성군", "880"),
    JEONNAM_WANDO("완도군", "890"),
    JEONNAM_JINDO("진도군", "900"),
    JEONNAM_SINAN("신안군", "910"),

    // 경상북도
    GYEONGBUK_POHANG("포항시", "110"),
    GYEONGBUK_GYEONGJU("경주시", "130"),
    GYEONGBUK_GIMCHEON("김천시", "150"),
    GYEONGBUK_ANDONG("안동시", "170"),
    GYEONGBUK_GUMI("구미시", "190"),
    GYEONGBUK_YEONGJU("영주시", "210"),
    GYEONGBUK_YEONGCHEON("영천시", "230"),
    GYEONGBUK_SANGJU("상주시", "250"),
    GYEONGBUK_MUNGYEONG("문경시", "280"),
    GYEONGBUK_GYEONGSAN("경산시", "290"),
    GYEONGBUK_UISEONG("의성군", "730"),
    GYEONGBUK_CHEONGSONG("청송군", "750"),
    GYEONGBUK_YEONGYANG("영양군", "760"),
    GYEONGBUK_YEONGDEOK("영덕군", "770"),
    GYEONGBUK_CHEONGDO("청도군", "820"),
    GYEONGBUK_GORYEONG("고령군", "830"),
    GYEONGBUK_SEONGJU("성주군", "840"),
    GYEONGBUK_CHILGOK("칠곡군", "850"),
    GYEONGBUK_YECHEON("예천군", "900"),
    GYEONGBUK_BONGHWA("봉화군", "920"),
    GYEONGBUK_ULJIN("울진군", "930"),
    GYEONGBUK_ULLEUNG("울릉군", "940"),

    // 경상남도
    GYEONGNAM_JINJU("진주시", "170"),
    GYEONGNAM_TONGYEONG("통영시", "220"),
    GYEONGNAM_SACHEON("사천시", "240"),
    GYEONGNAM_GIMHAE("김해시", "250"),
    GYEONGNAM_MIRYANG("밀양시", "270"),
    GYEONGNAM_GEOJE("거제시", "310"),
    GYEONGNAM_YANGSAN("양산시", "330"),
    GYEONGNAM_UIRYEONG("의령군", "720"),
    GYEONGNAM_HAMAN("함안군", "730"),
    GYEONGNAM_CHANGNYEONG("창녕군", "740"),
    GYEONGNAM_GOSEONG_GYEONGNAM("고성군", "820"),
    GYEONGNAM_NAMHAE("남해군", "840"),
    GYEONGNAM_HADONG("하동군", "850"),
    GYEONGNAM_SANCHEONG("산청군", "860"),
    GYEONGNAM_HAMYANG("함양군", "870"),
    GYEONGNAM_GEOCHANG("거창군", "880"),

    // 제주도
    JEJU_JEJU("제주시", "110"),
    JEJU_SEOGWIPO("서귀포시", "130");

    private final String name;
    private final String code;

    SigunguCode(String name, String code) {
        this.name = name;
        this.code = code;
    }

    public static String parseAddressToCode(String addr1) {
        if (addr1 == null || addr1.isEmpty()) {
            return "110";
        }

        String addrClean = addr1.replace("서울특별시", "").replace("서울", "")
                .replace("부산광역시", "").replace("부산", "")
                .replace("대구광역시", "").replace("대구", "")
                .replace("인천광역시", "").replace("인천", "")
                .replace("광주광역시", "").replace("광주", "")
                .replace("대전광역시", "").replace("대전", "")
                .replace("울산광역시", "").replace("울산", "")
                .replace("세종특별자치시", "").replace("세종", "")
                .replace("경기도", "").replace("강원도", "")
                .replace("충청북도", "").replace("충북", "")
                .replace("충청남도", "").replace("충남", "")
                .replace("전라북도", "").replace("전북", "")
                .replace("전라남도", "").replace("전남", "")
                .replace("경상북도", "").replace("경북", "")
                .replace("경상남도", "").replace("경남", "")
                .replace("제주특별자치도", "").replace("제주", "")
                .trim();

        // 지역별 우선순위로 검색 (서울 -> 부산 -> 대구 등 순서)
        for (SigunguCode sigungu : values()) {
            if (addrClean.contains(sigungu.name)) {
                return sigungu.code;
            }
        }

        return "110"; // 기본값: 종로구
    }

    public String getName() { return name; }
    public String getCode() { return code; }
}