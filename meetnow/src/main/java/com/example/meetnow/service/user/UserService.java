package com.example.meetnow.service.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.meetnow.Entity.Singuser;
import com.example.meetnow.Entity.User;
import com.example.meetnow.repository.user.UserIdRepository;
import com.example.meetnow.repository.user.UserSingRepository;

@Service
public class UserService {
	//UserSingRepository
	//Singuser
	
	
	    @Autowired
	    private UserSingRepository useridRepository;

	    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public boolean login(String userid, String password) {
    	
    	Singuser user = useridRepository.findByUserid(userid);
        
        if (user != null) {
            // 비밀번호 확인
            return encoder.matches(password, user.getPassword());
        }
        return false; // 사용자 없음
    }

    // 비밀번호 해시화 및 사용자 저장 메서드 (회원가입 등에서 사용)
    public void register(Singuser user) {
        // 비밀번호 해시화
        user.setPassword(encoder.encode(user.getPassword()));
        useridRepository.save(user); // DB에 사용자 저장
    }
}
