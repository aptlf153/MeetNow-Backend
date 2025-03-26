package com.example.meetnow.controller;

import java.sql.Timestamp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.meetnow.service.UserService;
import com.example.meetnow.util.JwtUtil;
import com.example.meetnow.dto.*;
import com.example.meetnow.response.JwtResponse;

@RestController
@CrossOrigin(origins = "http://localhost:5173") // React 앱의 URL
public class LoginController {

	@Autowired
	private JdbcTemplate jdbcTemplate; //DB와 상호작용하기
	
	@Autowired
	private UserService userService;
	

    @Autowired
    private JwtUtil jwtUtil; // JWT 유틸리티 클래스	
    

	//로그인
    @PostMapping("/api/users/login")
    public ResponseEntity<JwtResponse> login(@RequestBody DTO data) {
    	
        boolean success = userService.login(data.getUserid(), data.getPassword());

        if (success) {
            String accessjwt = jwtUtil.generateAccessToken(data.getUserid());
            String refreshjwt = jwtUtil.generateRefreshToken(data.getUserid());
            
            //토큰값이 넘어 오는지 디버그
            System.out.println("엑세스 토큰" + accessjwt);
            System.out.println("리프레시 토큰" + refreshjwt);
            
            // 현재 시간
            Timestamp createdDate = new Timestamp(System.currentTimeMillis());
            // 만료 시간 (7일 후)
            Timestamp expirationDate = new Timestamp(System.currentTimeMillis() + 604800000L); // 7일 후 (7일 * 24시간 * 60분 * 60초 * 1000밀리초)
	        
           String sql = "INSERT INTO refresh_tokens (userid, refresh_token,expiration_date,created_date) VALUES (?,?,?,?)";
           
           jdbcTemplate.update(sql, data.getUserid(),refreshjwt,createdDate,expirationDate);
           
            return ResponseEntity.ok(new JwtResponse(accessjwt)); // JWT 반환
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null); // 인증 실패 시 401 반환
        }
    }

    
}