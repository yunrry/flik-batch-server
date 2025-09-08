package yunrry.flik.batch.domain;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class PlaceReview {
    private String placeId;
    private double rating;
    private int reviewCount;
    private List<String> reviews;

}