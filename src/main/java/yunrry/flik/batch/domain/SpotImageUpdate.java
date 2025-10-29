package yunrry.flik.batch.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SpotImageUpdate {
    private Long id;
    private String imageUrls; // 콤마 구분 문자열
}