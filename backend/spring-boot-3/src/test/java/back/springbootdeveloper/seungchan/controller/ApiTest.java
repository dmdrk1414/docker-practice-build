package back.springbootdeveloper.seungchan.controller;

import back.springbootdeveloper.seungchan.controller.config.AttendanceListFromJson;
import back.springbootdeveloper.seungchan.controller.config.TestClassUtill;
import back.springbootdeveloper.seungchan.domain.*;
import back.springbootdeveloper.seungchan.dto.request.*;
import back.springbootdeveloper.seungchan.repository.*;
import back.springbootdeveloper.seungchan.service.TempUserService;
import back.springbootdeveloper.seungchan.service.TokenService;
import back.springbootdeveloper.seungchan.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.hibernate.sql.Update;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

// 메인 애플리케이션 클래스에 추가하는 애너테이션인 @SpringBootApplication이 있는 클래스 찾고
// 클래스에 포함되어 있는 빈을 찾은 다음, 테스트용 애플리케이션 컨텍스트라는 것을 만든다.
@SpringBootTest

// @AutoConfigureMockMvc는 MockMvc를 생성, 자동으로 구성하는 애너테이션
// MockMvc는 어플리케이션을 서버에 배포하지 하지 않고 테스트용 MVC 환경을 만들어 요청 및 전송, 응갇기능을 제공하는 유틸리티 클래스
// 컨트롤러를 테스트를 할때 사용되는 클래스
@AutoConfigureMockMvc // MockMvc 생성
public class ApiTest {
    // MockMVC 메서드 설명
    // perform() : 메서드는 요청을 전송하는 역할을 하는 메서드
    //              반환은 ResultActions 객체를 받으며
    //              ResultActions 객체는 반환값을 검증하고 확인한는 andExpect() 메서드를 제공

    // accept() : 메서드는 요청을 보낼 때 무슨 타입으로 응답을 받을지 결정하는 메서드
    //              JSON, XML 등 다양한 타입이 있지만, JSON을 받는다고 명시해둔다.

    // jsonPath("$[0].${필드명}) : JSON 응답값의 값을 가져오는 역할을 하는 메서드
    //                           0번째 배열에 들어있는 객체의 id, name값을 가져온다
    // ------------------------------------------------------------------

    // MockMvc 생성, MockMvc는 애플리케이션을 서버에 배포하지 않고, 테스트용 MVB 환경을 만들어 요청 및 전송, 응답 기능을 제공하는것
    // 컨트롤러를 테스트할 때 사용되는 클래스
    @Autowired
    protected MockMvc mockMvc;
    // ObjectMapper 클래스 - 직렬화, 역직렬화 할때 사용
    // 자바 객체를 JSON 데이터로 변환 OR JSON 데이터를 자바 객체로 변환
    // 직렬화 : 자바 시스템 내부에서 사용하는 객체를 외부에서 사용하도록 데이터를 변환하는 작업
    @Autowired
    protected ObjectMapper objectMapper; // 직렬화, 역직렬화를 위한 클래스
    @Autowired
    private SuggestionRepository suggestionRepository;
    @Autowired
    private WebApplicationContext context;

    // service
    @Autowired
    private TokenService tokenService;
    @Autowired
    private UserService userService;
    @Autowired
    private TempUserService tempUserService;

    // Repository
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserUtilRepository userUtilRepository;
    @Autowired
    private AttendanceStatusRepository attendanceStatusRepository;
    @Autowired
    private NumOfTodayAttendenceRepository numOfTodayAttendenceRepository;
    @Autowired
    private TempUserRepository tempUserRepository;

    private String token;
    private User user;
    private User userOb;
    private UserUtill userUtill;
    private AttendanceStatus attendanceStatus;

    @BeforeEach // 테스트 실행 전 실행하는 메서드
    public void mockMvcSetUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context) // MockMVB 설정
                .build();
    }

    //    token 발급
    @BeforeEach
    public void tokenSetUp() throws Exception {
        userRepository.deleteAll();
        userUtilRepository.deleteAll();
        attendanceStatusRepository.deleteAll();
        tempUserRepository.deleteAll();

        user = userRepository.save(TestClassUtill.makeUser());
        userRepository.updateId(user.getId(), 1L);
        user.setId(1L);

        userOb = userRepository.save(TestClassUtill.makeUserOb());
        userRepository.updateId(userOb.getId(), 2L);
        userOb.setId(2L);

        userUtill = userUtilRepository.save(TestClassUtill.makeUserUtill(user));

        attendanceStatus = attendanceStatusRepository.save(TestClassUtill.makeAttendanceStatus(user));

        String url = "/login";
        HttpServletRequest request = mockMvc.perform(
                post(url).param("email", user.getEmail()).param("password", user.getPassword())
        ).andReturn().getRequest();

        HttpServletResponse response = mockMvc.perform(
                post(url).param("email", user.getEmail()).param("password", user.getPassword())
        ).andReturn().getResponse();

        token = tokenService.createAccessAndRefreshToken(request, response, user.getEmail());
    }

    @DisplayName("건의 게시판 전체  조회 테스트")
    @Test
    public void fetchSuggestionsTest() throws Exception {
        // given
        this.suggestionRepository.deleteAll();

        final String url = "/suggestions";

        Suggestions saveSuggestions = suggestionRepository.save(TestClassUtill.makeSuggestions());
        // when
        final ResultActions resultActions = mockMvc.perform(get(url)
                .accept(MediaType.APPLICATION_JSON)
                .header("authorization", "Bearer " + token)); // token header에 담기

        // then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.suggestionLists[0].id").value(saveSuggestions.getId()))
                .andExpect(jsonPath("$.suggestionLists[0].classification").value(saveSuggestions.getClassification()))
                .andExpect(jsonPath("$.suggestionLists[0].title").value(saveSuggestions.getTitle()))
                .andExpect(jsonPath("$.suggestionLists[0].holidayPeriod").value(saveSuggestions.getHolidayPeriod()))
                .andExpect(jsonPath("$.suggestionLists[0].check").value(false))
                .andExpect(jsonPath("$.nuriKing").value(userUtill.isNuriKing()));
    }

    @DisplayName("건의 게시판 작성 테스트")
    @Test
    public void writeSuggestionTest() throws Exception {
        // given
        this.suggestionRepository.deleteAll();
        final String url = "/suggestions/write";
        Suggestions suggestionsRequest = TestClassUtill.makeSuggestions();

        // 객체 suggestionsRequest을 Json으로 직렬화
        final String requestBody = objectMapper.writeValueAsString(suggestionsRequest);

        // when
        ResultActions result = mockMvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(requestBody));

        // then
        result.andExpect(status().isOk());

        List<Suggestions> suggestionsList = suggestionRepository.findAll();
        assertThat(suggestionsList.size()).isEqualTo(1);
        assertThat(suggestionsList.get(0).getClassification()).isEqualTo(suggestionsRequest.getClassification());
        assertThat(suggestionsList.get(0).getTitle()).isEqualTo(suggestionsRequest.getTitle());
        assertThat(suggestionsList.get(0).getHolidayPeriod()).isEqualTo(suggestionsRequest.getHolidayPeriod());

    }

    @DisplayName("main page의 현재 재학 인원들 조회")
    @Test
    public void findAllYbUserTest() throws Exception {
        // given
        final String url = "/main/ybs";

        // when
        final ResultActions resultActions = mockMvc.perform(get(url)
                .accept(MediaType.APPLICATION_JSON)
                .header("authorization", "Bearer " + token) // token header에 담기
        );

        // then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].cntVacation").value(userUtill.getCntVacation()))
                .andExpect(jsonPath("$[0].name").value(userUtill.getName()))
                .andExpect(jsonPath("$[0].weeklyData").value(attendanceStatus.getWeeklyData()));
    }

    @DisplayName("메인 회원상세(일반, 졸업자) 조회")
    @Test
    public void fetchUserOfDetail2MainTest() throws Exception {
        // given
        final String url = "/main/detail/{id}";

        // when
        final ResultActions resultActions = mockMvc.perform(get(url, user.getId())
                .accept(MediaType.APPLICATION_JSON)
                .header("authorization", "Bearer " + token) // token header에 담기
        );


        // then
        resultActions
                .andExpect(jsonPath("$.name").value(user.getName()))
                .andExpect(jsonPath("$.major").value(user.getMajor()))
                .andExpect(jsonPath("$.studentId").value(user.getStudentId()))
                .andExpect(jsonPath("$.phoneNum").value(user.getPhoneNum()))
                .andExpect(jsonPath("$.hobby").value(user.getHobby()))
                .andExpect(jsonPath("$.specialtySkill").value(user.getSpecialtySkill()))
                .andExpect(jsonPath("$.mbti").value(user.getMbti()))
                .andExpect(jsonPath("$.userId").value(user.getId()))
                .andExpect(jsonPath("$.ob").value(user.isOb()))
                .andExpect(jsonPath("$.nuriKing").value(true));
    }

    @DisplayName("main page 졸업 인원들의 정보")
    @Test
    public void findAllObUserTest() throws Exception {
        // given
        final String url = "/main/obs";

        // when
        final ResultActions resultActions = mockMvc.perform(get(url)
                .accept(MediaType.APPLICATION_JSON)
                .header("authorization", "Bearer " + token) // token header에 담기
        );

        // then
        resultActions
                .andExpect(jsonPath("$[0].obUserList[0].name").value(userOb.getName()))
                .andExpect(jsonPath("$[0].obUserList[0].yearOfRegistration").value(userOb.getYearOfRegistration()))
                .andExpect(jsonPath("$[0].obUserList[0].phoneNum").value(userOb.getPhoneNum()));
    }

    @DisplayName("출석 번호 입력 API 테스트")
    @Test
    public void attendanceNumberControllerTest() throws Exception {
        // given
        final String url = "/attendance/number";
        List<NumOfTodayAttendence> numOfTodayAttendenceList = numOfTodayAttendenceRepository.findAll();
        NumOfTodayAttendence numOfTodayAttendence = numOfTodayAttendenceList.get(numOfTodayAttendenceList.size() - 1);
        String num = numOfTodayAttendence.getCheckNum();


        AttendanceNumberRequest attendanceNumberRequest = new AttendanceNumberRequest();
        attendanceNumberRequest.setNumOfAttendance(num);
        // when
        ResultActions result = mockMvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(attendanceNumberRequest))
                .header("authorization", "Bearer " + token) // token header에 담기
        );

        // then
        result
                .andExpect(jsonPath("$.passAtNow").value(true));
    }

    @DisplayName("내 정보를 조회한다.")
    @Test
    public void findMypageTest() throws Exception {
        // given
        final String url = "/mypage";

        // when
        ResultActions result = mockMvc.perform(get(url)
                .accept(MediaType.APPLICATION_JSON)
                .header("authorization", "Bearer " + token) // token header에 담기
        );

        // then
        result
                .andExpect(jsonPath("$.name").value(user.getName()))
                .andExpect(jsonPath("$.phoneNum").value(user.getPhoneNum()))
                .andExpect(jsonPath("$.major").value(user.getMajor()))
                .andExpect(jsonPath("$.gpa").value(user.getGpa()))
                .andExpect(jsonPath("$.address").value(user.getAddress()))
                .andExpect(jsonPath("$.specialtySkill").value(user.getSpecialtySkill()))
                .andExpect(jsonPath("$.hobby").value(user.getHobby()))
                .andExpect(jsonPath("$.mbti").value(user.getMbti()))
                .andExpect(jsonPath("$.studentId").value(user.getStudentId()))
                .andExpect(jsonPath("$.birthDate").value(user.getBirthDate()))
                .andExpect(jsonPath("$.advantages").value(user.getAdvantages()))
                .andExpect(jsonPath("$.disadvantage").value(user.getDisadvantage()))
                .andExpect(jsonPath("$.selfIntroduction").value(user.getSelfIntroduction()))
                .andExpect(jsonPath("$.photo").value(user.getPhoto()))
                .andExpect(jsonPath("$.yearOfRegistration").value(user.getYearOfRegistration()))
                .andExpect(jsonPath("$.ob").value(user.isOb()));
    }

    @DisplayName("나의 정보를 업데이트 한다.")
    @Test
    public void updateMypageTest() throws Exception {
        // given
        final String url = "/mypage/update";
        final String nameUpdate = "업데이트한_이름";
        User updateUser = user;
        updateUser.setName(nameUpdate);
        UpdateUserFormRequest requestUserForm = new UpdateUserFormRequest(
                user.getName(),
                user.getPhoneNum(),
                user.getMajor(),
                user.getGpa(),
                user.getAddress(),
                user.getSpecialtySkill(),
                user.getHobby(),
                user.getMbti(),
                user.getStudentId(),
                user.getBirthDate(),
                user.getAdvantages(),
                user.getDisadvantage(),
                user.getSelfIntroduction(),
                user.getPhoto()
        );
        System.out.println("requestUserForm = " + requestUserForm);

        // 객체 suggestionsRequest을 Json으로 직렬화
        final String requestBody = objectMapper.writeValueAsString(requestUserForm);

        // when
        ResultActions result = mockMvc.perform(put(url)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .header("authorization", "Bearer " + token) // token header에 담기
        );

        updateUser = userRepository.findById(user.getId())
                .orElseThrow(IllegalArgumentException::new);

        // then
        result
                .andExpect(status().isOk()); // 변경된 것이 잘되었는지 확인
        assertThat(updateUser.getName()).isEqualTo(nameUpdate);
        // then
    }

    @DisplayName("회원들이 휴가을 휴가 신청 API")
    @Test
    public void applyVacationTest() throws Exception {
        // given
        final String url = "/vacations/request";
        String preVacationDate[] = new String[]{"2023-07-28", "2023-07-30"};
        int cntUseOfVacation = 2;
        VacationRequest vacationRequest = new VacationRequest(preVacationDate, cntUseOfVacation);

        // 객체 suggestionsRequest을 Json으로 직렬화
        final String requestBody = objectMapper.writeValueAsString(vacationRequest);

        // when
        ResultActions result = mockMvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .header("authorization", "Bearer " + token) // token header에 담기
        );

        AttendanceStatus attendanceStatus = attendanceStatusRepository.findByUserId(user.getId());

        // then
        result
                .andExpect(status().isOk());
        assertThat(attendanceStatus.getVacationDates()).contains(preVacationDate);
    }

    @DisplayName("회원들의 휴가 날짜 조회 테스트")
    @Test
    public void findVacationTest() throws Exception {
        // given
        final String url = "/vacations";

        MockHttpServletResponse response = mockMvc.perform(get(url)
                .accept(MediaType.APPLICATION_JSON)
                .header("authorization", "Bearer " + token) // token header에 담기
        ).andReturn().getResponse();
        String responseStr = response.getContentAsString();

        // when
        ResultActions resultActions = mockMvc.perform(get(url)
                .accept(MediaType.APPLICATION_JSON)
                .header("authorization", "Bearer " + token) // token header에 담기
        );

        AttendanceListFromJson attendanceListFromJson = objectMapper.readValue(responseStr, AttendanceListFromJson.class);
        // then
        resultActions
                .andExpect(jsonPath("$.absences").value(attendanceListFromJson.getAbsences()))
                .andExpect(jsonPath("$.beforeVacationDate").value(attendanceListFromJson.getBeforeVacationDate()))
                .andExpect(jsonPath("$.preVacationDate").value(attendanceListFromJson.getPreVacationDate())
                );

    }

    @DisplayName("실장이 회원들에게 휴가 갯수 부여가능")
    @Test
    public void vacationCountTest() throws Exception {
        // given
        final String url = "/vacations/count";
        int cntVacation = userUtill.getCntVacation();
        int addCntVacation = 1;
        VacationCountRequest vacationCountRequest = new VacationCountRequest(addCntVacation, user.getId());
        int cntVacationOfAfterAdd = cntVacation + addCntVacation;

        // when
        // 객체 suggestionsRequest을 Json으로 직렬화
        final String requestBody = objectMapper.writeValueAsString(vacationCountRequest);

        ResultActions result = mockMvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .header("authorization", "Bearer " + token) // token header에 담기
        );

        userUtill = userUtilRepository.findByUserId(user.getId());

        // then
        result.andExpect(status().isOk());
        assertThat(userUtill.getCntVacation()).isEqualTo(cntVacationOfAfterAdd);
    }

    @DisplayName("휴가 신청 페이지 조회")
    @Test
    public void findsVacationRequestTest() throws Exception {
        // given
        final String url = "/vacations/request";

        MockHttpServletResponse response = mockMvc.perform(get(url)
                .accept(MediaType.APPLICATION_JSON)
                .header("authorization", "Bearer " + token) // token header에 담기
        ).andReturn().getResponse();
        String responseStr = response.getContentAsString();
        int cntVacation = userUtill.getCntVacation();

        // when
        ResultActions resultActions = mockMvc.perform(get(url)
                .accept(MediaType.APPLICATION_JSON)
                .header("authorization", "Bearer " + token) // token header에 담기
        );

        AttendanceListFromJson attendanceListFromJson = objectMapper.readValue(responseStr, AttendanceListFromJson.class);
        // then
        resultActions
                .andExpect(jsonPath("$.absences").value(attendanceListFromJson.getAbsences()))
                .andExpect(jsonPath("$.beforeVacationDate").value(attendanceListFromJson.getBeforeVacationDate()))
                .andExpect(jsonPath("$.preVacationDate").value(attendanceListFromJson.getPreVacationDate()))
                .andExpect(jsonPath("$.cntVacation").value(cntVacation));
    }

    @DisplayName("새로운 회원들의 회원가입 절차")
    @Test
    public void newUserSignUpTest() throws Exception {
        // given
        final String url = "/sign";
        String email = "new@new.com";
        String password = new BCryptPasswordEncoder().encode("1234");
        TempUser newUser = TestClassUtill.makeNewUserOb(email, password);

        TempUserFormRequest requestUserForm = new TempUserFormRequest(
                newUser.getName(),
                newUser.getPhoneNum(),
                newUser.getMajor(),
                newUser.getGpa(),
                newUser.getAddress(),
                newUser.getSpecialtySkill(),
                newUser.getHobby(),
                newUser.getMbti(),
                newUser.getStudentId(),
                newUser.getBirthDate(),
                newUser.getAdvantages(),
                newUser.getDisadvantage(),
                newUser.getSelfIntroduction(),
                newUser.getPhoto(),
                newUser.getEmail(),
                newUser.getPassword()
        );

        // 객체 suggestionsRequest을 Json으로 직렬화
        final String requestBody = objectMapper.writeValueAsString(requestUserForm);

        // when
        ResultActions result = mockMvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .header("authorization", "Bearer " + token) // token header에 담기
        );

        TempUser newUserOfTempDb = tempUserRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("not found: ")); // 찾아서 없으면 예외처리.;;

        boolean resultPassword = new BCryptPasswordEncoder().matches(password, newUserOfTempDb.getPassword());

        // then
        assertThat(newUser.getEmail()).isEqualTo(email);
        assertThat(resultPassword).isTrue();
        assertThat(newUser.getName()).isEqualTo(newUserOfTempDb.getName());

        // then
        result
                .andExpect(status().isOk());
    }

    @DisplayName("모든 신청 유저들의 정보를 볼수 있는 API 테스트")
    @Test
    public void findAllNewUsersTest() throws Exception {
        // given
        final String url = "/new-users";
        String email_1 = "new@new.com_1";
        String email_2 = "new@new.com_2";
        String password_1 = new BCryptPasswordEncoder().encode("1234");
        String password_2 = new BCryptPasswordEncoder().encode("1234");
        TempUser tempUser_1 = TestClassUtill.makeNewUserOb(email_1, password_1);
        TempUser tempUser_2 = TestClassUtill.makeNewUserOb(email_2, password_2);

        tempUserRepository.save(tempUser_1);
        tempUserRepository.save(tempUser_2);

        // when
        ResultActions result = mockMvc.perform(get(url)
                .accept(MediaType.APPLICATION_JSON)
                .header("authorization", "Bearer " + token)); // token header에 담기

        // then
        result
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(tempUser_1.getId()))
                .andExpect(jsonPath("$[0].name").value(tempUser_1.getName()));
    }

    @DisplayName("신청 유저들의 개별 정보를 볼수 있는 API 테스트")
    @Test
    public void findNewUsersTest() throws Exception {
        // given
        String email_1 = "new@new.com_1";
        String password_1 = new BCryptPasswordEncoder().encode("1234");
        TempUser tempUser_1 = TestClassUtill.makeNewUserOb(email_1, password_1);

        TempUser tempUserDB = tempUserRepository.save(tempUser_1);
        final String url = "/new-users/" + tempUserDB.getId();

        // when
        ResultActions result = mockMvc.perform(get(url)
                .accept(MediaType.APPLICATION_JSON)
                .header("authorization", "Bearer " + token)); // token header에 담기

        // then
        result
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tempUser.name").value(tempUserDB.getName()))
                .andExpect(jsonPath("$.tempUser.phoneNum").value(tempUserDB.getPhoneNum()))
                .andExpect(jsonPath("$.tempUser.major").value(tempUserDB.getMajor()))
                .andExpect(jsonPath("$.tempUser.gpa").value(tempUserDB.getGpa()))
                .andExpect(jsonPath("$.tempUser.address").value(tempUserDB.getAddress()))
                .andExpect(jsonPath("$.tempUser.specialtySkill").value(tempUserDB.getSpecialtySkill()))
                .andExpect(jsonPath("$.tempUser.mbti").value(tempUserDB.getMbti()))
                .andExpect(jsonPath("$.tempUser.studentId").value(tempUserDB.getStudentId()))
                .andExpect(jsonPath("$.tempUser.birthDate").value(tempUserDB.getBirthDate()))
                .andExpect(jsonPath("$.tempUser.advantages").value(tempUserDB.getAdvantages()))
                .andExpect(jsonPath("$.tempUser.disadvantage").value(tempUserDB.getDisadvantage()))
                .andExpect(jsonPath("$.tempUser.selfIntroduction").value(tempUserDB.getSelfIntroduction()))
                .andExpect(jsonPath("$.tempUser.photo").value(tempUserDB.getPhoto()))
                .andExpect(jsonPath("$.tempUser.yearOfRegistration").value(tempUserDB.getYearOfRegistration()))
                .andExpect(jsonPath("$.tempUser.email").value(tempUserDB.getEmail()))
                .andExpect(jsonPath("$.tempUser.password").value(tempUserDB.getPassword()))
                .andExpect(jsonPath("$.tempUser.regularMember").value(tempUserDB.isRegularMember()))
                .andExpect(jsonPath("$.tempUser.ob").value(tempUserDB.isOb()))
                .andExpect(jsonPath("$.nuriKing").value(true));
    }

    @DisplayName("신청 개별 유저의 승락을 하는 메서드 테스트")
    @Test
    public void acceptNewUserOfKingTest() throws Exception {
        // given
        String email_1 = "new@new.com_1";
        String password_1 = new BCryptPasswordEncoder().encode("1234");
        TempUser tempUser_1 = TestClassUtill.makeNewUserOb(email_1, password_1);

        TempUser tempUserDB = tempUserRepository.save(tempUser_1);
        final String url = "/new-users/" + tempUserDB.getId() + "/acceptance";

        NewUserApprovalRequest newUserApprovalRequest = new NewUserApprovalRequest(tempUserDB.getId());

        // 객체 suggestionsRequest을 Json으로 직렬화
        final String requestBody = objectMapper.writeValueAsString(newUserApprovalRequest);

        // when
        ResultActions result = mockMvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .header("authorization", "Bearer " + token) // token header에 담기
        );

        User newUser = userService.findByEmail(email_1);

        // then
        result
                .andExpect(status().isOk());
        assertThat(newUser.getEmail()).isEqualTo(tempUserDB.getEmail());

    }

    @DisplayName("신청 개별 유저의 거절 하는 메서드 테스트")
    @Test
    public void rejectNewUserOfKingTest() throws Exception {
        // given
        String email_1 = "new@new.com_1";
        String password_1 = new BCryptPasswordEncoder().encode("1234");
        TempUser tempUser_1 = TestClassUtill.makeNewUserOb(email_1, password_1);

        TempUser tempUserDB = tempUserRepository.save(tempUser_1);
        final String url = "/new-users/" + tempUserDB.getId() + "/reject";

        NewUserApprovalRequest newUserApprovalRequest = new NewUserApprovalRequest(tempUserDB.getId());

        // 객체 suggestionsRequest을 Json으로 직렬화
        final String requestBody = objectMapper.writeValueAsString(newUserApprovalRequest);

        // when
        ResultActions result = mockMvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .header("authorization", "Bearer " + token) // token header에 담기
        );

        // then
        result
                .andExpect(status().isOk());
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            tempUserService.findNewUsers(tempUserDB.getId());
        });

    }
}
