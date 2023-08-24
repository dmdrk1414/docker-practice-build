package back.springbootdeveloper.seungchan.dto.response;

import back.springbootdeveloper.seungchan.domain.User;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MyPageResponse {
    private String name;
    private String phoneNum;
    private String major;
    private String gpa;
    private String address;
    private String specialtySkill;
    private String hobby;
    private String mbti;
    private String studentId;
    private String birthDate;
    private String advantages;
    private String disadvantage;
    private String selfIntroduction;
    private String photo;
    private boolean isOb;
    private String yearOfRegistration;

    public MyPageResponse(User user) {
        this.name = user.getName();
        this.phoneNum = user.getPhoneNum();
        this.major = user.getMajor();
        this.gpa = user.getGpa();
        this.address = user.getAddress();
        this.specialtySkill = user.getSpecialtySkill();
        this.hobby = user.getHobby();
        this.mbti = user.getMbti();
        this.studentId = user.getStudentId();
        this.birthDate = user.getBirthDate();
        this.advantages = user.getAdvantages();
        this.disadvantage = user.getDisadvantage();
        this.selfIntroduction = user.getSelfIntroduction();
        this.photo = user.getPhoto();
        this.isOb = user.isOb();
        this.yearOfRegistration = user.getYearOfRegistration();
    }
}
