package yunrry.flik.batch.domain;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

// API 호출 이력 도메인
@Data
@Builder
public class ApiCallHistory {
    private String contentTypeId;
    private String areaCode;
    private int lastPageNo;
    private int pageSize;
    private int totalCollected;
    private LocalDateTime lastCallTime;
}