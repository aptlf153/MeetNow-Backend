package com.example.meetnow.controller;

import java.sql.Timestamp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.meetnow.service.UserService;
import com.example.meetnow.util.JwtUtil;

import jakarta.servlet.http.HttpServletResponse;

import com.example.meetnow.dto.*;
import com.example.meetnow.repository.CookieUtil;

import org.springframework.http.HttpHeaders;

@RestController
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true") // React 앱의 URL
public class LoginController {

	@Autowired
	private JdbcTemplate jdbcTemplate; //DB와 상호작용하기
	
	@Autowired
	private UserService userService;
	
    @Autowired
    private JwtUtil jwtUtil; // JWT 유틸리티 클래스	
    
    @Autowired
    private CookieUtil cookieUtil;
    
	//로그인
    @PostMapping("/api/users/login")
    public ResponseEntity<Boolean> login(@RequestBody DTO data, HttpServletResponse response) {
        boolean success = userService.login(data.getUserid(), data.getPassword());

        if (success) {
            String accessToken = jwtUtil.generateAccessToken(data.getUserid());
            String refreshToken = jwtUtil.generateRefreshToken(data.getUserid());

            // 현재 시간
            Timestamp createdDate = new Timestamp(System.currentTimeMillis());
            // 만료 시간 (7일 후)
            Timestamp expirationDate = new Timestamp(System.currentTimeMillis() + 604800000); // 7일 후

            String sql = "INSERT INTO refresh_tokens (userid, refreshtoken,expiration_date,created_date) VALUES (?,?,?,?)";
            jdbcTemplate.update(sql, data.getUserid(), refreshToken, createdDate, expirationDate);

            //쿠키 만들기 컴포넌트
            cookieUtil.addCookies(response, accessToken, refreshToken);

            // 응답 본문 반환
            return ResponseEntity.ok(true);
        } else {
            // 로그인 실패 시 UNAUTHORIZED 응답 반환
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
    }

    
}