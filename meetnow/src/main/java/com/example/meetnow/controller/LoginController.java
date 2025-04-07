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

            ResponseCookie accessCookie = ResponseCookie.from("accessToken", accessToken)
                    .httpOnly(true) // 클라이언트에서 접근 불가
                    .path("/")
                    .maxAge(60) //10분
                    .sameSite("Lax") // 같은 도메인에서만
                    .secure(false) // 개발 환경에서는 false로 설정
                    .domain("localhost") // 로컬 환경에서만 유효
                    .build();
            response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
            
            ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", refreshToken)
                    .httpOnly(true) // 클라이언트에서 접근 불가
                    .path("/")
                    .maxAge(60 * 24 * 7)
                    .sameSite("Lax") // 같은 도메인에서만
                    .secure(false) // 개발 환경에서는 false로 설정
                    .domain("localhost") // 로컬 환경에서만 유효
                    .build();
            response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());     

            // 응답 본문 반환
            return ResponseEntity.ok(true);
        } else {
            // 로그인 실패 시 UNAUTHORIZED 응답 반환
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
    }

    
}