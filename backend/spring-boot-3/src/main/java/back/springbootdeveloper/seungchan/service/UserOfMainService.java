package back.springbootdeveloper.seungchan.service;

import back.springbootdeveloper.seungchan.domain.AttendanceStatus;
import back.springbootdeveloper.seungchan.domain.ObUser;
import back.springbootdeveloper.seungchan.domain.User;
import back.springbootdeveloper.seungchan.domain.UserUtill;
import back.springbootdeveloper.seungchan.dto.response.ObUserOfMainResponse;
import back.springbootdeveloper.seungchan.dto.response.YbUserOfMainResponse;
import back.springbootdeveloper.seungchan.repository.AttendanceStatusRepository;
import back.springbootdeveloper.seungchan.repository.UserRepository;
import back.springbootdeveloper.seungchan.repository.UserUtilRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor // final이 붙거나  @NotNull이 붙는 필드의 생성자 추가
@Service
public class UserOfMainService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AttendanceStatusRepository attendanceStatusRepository;
    @Autowired
    private UserUtilRepository userUtilRepository;

    public List<YbUserOfMainResponse> findAllByIsOb(boolean isOb) {
        List<YbUserOfMainResponse> responseList = new ArrayList<>();
        List<User> users = userRepository.findAll();
        for (int id = 1; id < users.size() + 1; id++) {
            boolean isObUser = users.get(id - 1).isOb();
            if (isObUser == isOb) {
                Long longId = (long) id;
                System.out.println("longId = " + longId);
                UserUtill userUtill = userUtilRepository.findByUserId(longId);
                AttendanceStatus attendanceStatus = attendanceStatusRepository.findByUserId(longId);
                responseList.add(new YbUserOfMainResponse(attendanceStatus, userUtill));
            }
        }
        return responseList;
    }

    public List<ObUser> findAllObUser() {
        List<ObUser> responseList = new ArrayList<>();
        List<User> users = userRepository.findAll();
        for (int id = 1; id < users.size() + 1; id++) {
            boolean isObUser = users.get(id - 1).isOb();
            if (isObUser) {
                int indexOfuser = id - 1;
                User user = users.get(indexOfuser);
                responseList.add(new ObUser(user));
            }
        }
        return responseList;
    }

}
