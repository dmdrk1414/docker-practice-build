package back.springbootdeveloper.seungchan.dto.response;

import back.springbootdeveloper.seungchan.domain.Suggestions;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
public class SuggestionList {
    private Long id;
    private String classification;
    private String title;
    private boolean isCheck;
    private String holidayPeriod;

    public SuggestionList(Suggestions suggestions) {
        this.id = suggestions.getId();
        this.classification = suggestions.getClassification();
        this.title = suggestions.getTitle();
        this.isCheck = suggestions.isCheck();
        this.holidayPeriod = suggestions.getHolidayPeriod();
    }
}
