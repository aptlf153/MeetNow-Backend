package com.example.meetnow.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.meetnow.service.RegisterUser;
import com.example.meetnow.service.hashpasswordService;
import com.example.meetnow.dto.*;

@RestController
@CrossOrigin(origins = "http://localhost:5173") // React 앱의 URL
public class SingupController {

	@Autowired
	private JdbcTemplate jdbcTemplate; //DB와 상호작용하기
	
	@Autowired
	private RegisterUser registerUser;


	@Autowired
	private hashpasswordService hashpasswordService;
    
    //회원가입 성공
    @PostMapping("/api/users/signup")
    public String signup(@RequestBody DTO data) {
        
            String hashPassword = hashpasswordService.hashPassword(data);
            // 데이터베이스에 업데이트
            String sql = "INSERT INTO user (name,nickname,gender,userid,password,phone,email) VALUES (?,?,?,?,?,?,?)";
            jdbcTemplate.update(sql, data.getName(),data.getNickname(),data.getGender(),data.getUserid(),hashPassword,data.getPhone(),data.getEmail());

            return data.toString();
    }
    
    //회원가입 체크
    @PostMapping("/api/users/signupcheck")
    public int signupCheck(@RequestBody DTO data) {
        
    	boolean idCheck = registerUser.idCheck(data.getUserid());
    	boolean emailCheck = registerUser.emailCheck(data.getEmail());
    	boolean nickCheck = registerUser.nicknameCheck(data.getNickname());
    	
    	if(idCheck) {
    		return 1;
    	}
    	else if(emailCheck) 
    	{
    		return 2;
    	}
    	else if(nickCheck)
    	{
    		return 3;
    	}
    	
    	return 0;
    }
    
}