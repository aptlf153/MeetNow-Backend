package com.example.meetnow.controller.admin;
import java.sql.Timestamp;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.meetnow.util.cookie.CookieUtil;
import com.example.meetnow.util.jwt.JwtUtil;
import com.example.meetnow.dto.common.DTO;
import com.example.meetnow.service.auth.AdminLoginService;

import jakarta.servlet.http.HttpServletResponse; // HttpServletResponse 임포트

@RestController
public class AdminAuthController {

    private final AdminLoginService adminLoginService;
    private final JwtUtil jwtUtil;
    private final CookieUtil cookieUtil;
    private final JdbcTemplate jdbcTemplate;

    public AdminAuthController(AdminLoginService adminLoginService, JwtUtil jwtUtil,
                               CookieUtil cookieUtil, JdbcTemplate jdbcTemplate) {
        this.adminLoginService = adminLoginService;
        this.jwtUtil = jwtUtil;
        this.cookieUtil = cookieUtil;
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostMapping("/api/admin/login")
    public ResponseEntity<Boolean> login(@RequestBody DTO data, HttpServletResponse response) {
        boolean success = adminLoginService.verifyLogin(data.getUserid(), data.getPassword());

        if (!success) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(false);
        }

        String accessToken = jwtUtil.generateAccessToken(data.getUserid());
        String refreshToken = jwtUtil.generateRefreshToken(data.getUserid());

        Timestamp now = new Timestamp(System.currentTimeMillis());
        Timestamp expire = new Timestamp(System.currentTimeMillis() + 604800000L); // 7일

        jdbcTemplate.update("DELETE FROM refresh_tokens WHERE userid = ?", data.getUserid());
        jdbcTemplate.update(
            "INSERT INTO refresh_tokens (userid, refreshtoken, expiration_date, created_date) VALUES (?, ?, ?, ?)",
            data.getUserid(), refreshToken, expire, now
        );

        cookieUtil.addCookies(response, accessToken, refreshToken);

        return ResponseEntity.ok(true);
    }
}
