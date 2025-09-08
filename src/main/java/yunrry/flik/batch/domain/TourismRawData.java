package yunrry.flik.batch.domain;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
public class TourismRawData {
    private String contentId;
    private String contentTypeId;
    private String contentTypeName;
    private String title;
    private String addr1;
    private String addr2;
    private String firstImage;
    private String firstImage2;
    private String mapX;
    private String mapY;
    private String areaCode;
    private String sigunguCode;
    private String cat1;
    private String cat2;
    private String cat3;
    private String createdTime;
    private String modifiedTime;
    private String tel;
    private String zipcode;
    private String overview;
    private String source;
    private LocalDateTime collectedAt;

    // 공통 상세 정보
    private String usetime;
    private String restdate;
    private String parking;
    private String parkingfee;
    private String infocenter;
    private String chkbabycarriage;
    private String chkpet;
    private String chkcreditcard;

    // 원본 데이터
    private Map<String, Object> rawData;

    // 도메인별 특화 필드들 (Map으로 관리)
    private Map<String, String> domainFields;
}