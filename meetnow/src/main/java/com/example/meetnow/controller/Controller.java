package com.example.meetnow.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
//import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.meetnow.service.RegisterUser;
import com.example.meetnow.service.UserService;
import com.example.meetnow.service.hashpasswordService;
import com.example.meetnow.util.JwtUtil;
import com.example.meetnow.dto.*;
import com.example.meetnow.response.JwtResponse;

@RestController
@CrossOrigin(origins = "http://localhost:5173") // React 앱의 URL
public class Controller {

	@Autowired
	private JdbcTemplate jdbcTemplate; //DB와 상호작용하기
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private RegisterUser registerUser;
	
//    @Autowired
//    private AuthenticationManager authenticationManager; // 인증 관리자

    @Autowired
    private JwtUtil jwtUtil; // JWT 유틸리티 클래스	
    

    //서버 확인용
	@GetMapping("/")
	@ResponseBody
	public String HellowAPI() {
		return "HellowAPI";
	}
	
	@Autowired
	private hashpasswordService hashpasswordService;
	
	
	//로그인
    @PostMapping("/api/users/login")
    public ResponseEntity<JwtResponse> login(@RequestBody DTO data) {
    	
        boolean success = userService.login(data.getUserid(), data.getPassword());

        if (success) {
            String jwt = jwtUtil.generateToken(data.getUserid());
            
            //토큰값이 넘어 오는지 디버그
            System.out.println("토큰" + jwt);
            
            return ResponseEntity.ok(new JwtResponse(jwt)); // JWT 반환
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null); // 인증 실패 시 401 반환
        }
    }

    //회원가입 성공
    @PostMapping("/api/users/signup")
    public String signup(@RequestBody DTO data) {
        
            System.out.println(data.toString());
        	
            String hashPassword = hashpasswordService.hashPassword(data);
            // 데이터베이스에 업데이트
            String sql = "INSERT INTO test_table (name,nickname,gender,userid,password,phone,email) VALUES (?,?,?,?,?,?,?)";
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