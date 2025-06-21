package com.example.meetnow.service.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.example.meetnow.dto.common.DTO;
import com.example.meetnow.repository.user.UserIdRepository;
import com.example.meetnow.repository.user.UseremailRepository;
import com.example.meetnow.repository.user.UsernicknamelRepository;

@Service
public class RegisterUserService {

    @Autowired
    private UserIdRepository useridRepository;

    @Autowired
    private UseremailRepository useremailRepository;

    @Autowired
    private UsernicknamelRepository usernicknamelRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private HashpasswordService hashpasswordService;

    // 실제 회원가입 처리
    public void register(DTO data) {
        String hashPassword = hashpasswordService.hashPassword(data);
        String sql = "INSERT INTO user (name, nickname, gender, userid, password, phone, email) VALUES (?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, data.getName(), data.getNickname(), data.getGender(),
                data.getUserid(), hashPassword, data.getPhone(), data.getEmail());
    }

    // 회원가입 중복 체크
    public int checkDuplicate(DTO data) {
        if (idCheck(data.getUserid())) return 1;
        if (emailCheck(data.getEmail())) return 2;
        if (nicknameCheck(data.getNickname())) return 3;
        return 0;
    }

    // 아이디 중복 확인
    public Boolean idCheck(String userid) {
        return useridRepository.findByUserid(userid) != null;
    }

    // 이메일 중복 확인
    public Boolean emailCheck(String useremail) {
        return useremailRepository.findByemail(useremail) != null;
    }

    // 닉네임 중복 확인
    public Boolean nicknameCheck(String nickname) {
        return usernicknamelRepository.findBynickname(nickname) != null;
    }
}
