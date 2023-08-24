package back.springbootdeveloper.seungchan.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Table(name = "user_utill")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
@Builder
public class UserUtill {
    @Id // id 필드를 기본키로 지정
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 기본키 자동으로 1씩 증가
    @Column(name = "id", updatable = false)
    private Long id;

    @Column(name = "user_id", nullable = false, updatable = false)
    private Long userId;

    @Column(name = "name", nullable = false, updatable = false)
    private String name;

    @Column(name = "cnt_vacation", nullable = false, updatable = false)
    private int cntVacation;

    @Column(name = "is_nuri_king", nullable = false, updatable = false)
    private boolean isNuriKing;

    @Column(name = "is_general_affairs", nullable = false, updatable = false)
    private boolean isGeneralAffairs;

    public UserUtill(Long id, Long userId, String name, int cntVacation, boolean isNuriKing) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.cntVacation = cntVacation;
        this.isNuriKing = isNuriKing;
    }

    public UserUtill(Long id, Long userId, String name, int cntVacation, boolean isNuriKing, boolean isGeneralAffairs) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.cntVacation = cntVacation;
        this.isNuriKing = isNuriKing;
        this.isGeneralAffairs = isGeneralAffairs;
    }

    @Override
    public String toString() {
        return "UserUtill{" +
                "id=" + id +
                ", userId=" + userId +
                ", name='" + name + '\'' +
                ", cntVacation=" + cntVacation +
                ", isNuriKing=" + isNuriKing +
                '}';
    }

    public void updateVacationNum(int resultVacationNum) {
        this.cntVacation = resultVacationNum;
    }
}
