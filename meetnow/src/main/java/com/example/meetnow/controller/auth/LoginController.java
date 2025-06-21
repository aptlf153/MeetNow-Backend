package com.example.meetnow.controller.auth;

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

import com.example.meetnow.util.cookie.CookieUtil;
import com.example.meetnow.util.jwt.JwtUtil;

import jakarta.servlet.http.HttpServletResponse;

import com.example.meetnow.dto.*;
import com.example.meetnow.dto.common.DTO;
import com.example.meetnow.service.user.UserService;

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

            Timestamp createdDate = new Timestamp(System.currentTimeMillis());
            Timestamp expirationDate = new Timestamp(System.currentTimeMillis() + 604800000); // 7일 후

            // 기존 토큰 삭제 (userid 기준)
            String deleteSql = "DELETE FROM refresh_tokens WHERE userid = ?";
            jdbcTemplate.update(deleteSql, data.getUserid());

            String insertSql = "INSERT INTO refresh_tokens (userid, refreshtoken,expiration_date,created_date) VALUES (?,?,?,?)";
            jdbcTemplate.update(insertSql, data.getUserid(), refreshToken, createdDate, expirationDate);
            
            
            // 쿠키 설정
            cookieUtil.addCookies(response, accessToken, refreshToken);

            return ResponseEntity.ok(true);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
    }

    
}