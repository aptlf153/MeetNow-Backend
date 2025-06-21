package com.example.meetnow.controller.auth;

import com.example.meetnow.dto.common.DTO;
import com.example.meetnow.service.auth.TokenService;
import com.example.meetnow.service.user.UserService;
import com.example.meetnow.util.cookie.CookieUtil;
import com.example.meetnow.util.jwt.JwtUtil;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class LoginController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CookieUtil cookieUtil;

    @Autowired
    private TokenService tokenService; // ✅ 토큰 서비스 주입

    @PostMapping("/api/users/login")
    public ResponseEntity<Boolean> login(@RequestBody DTO data, HttpServletResponse response) {
        boolean success = userService.login(data.getUserid(), data.getPassword());

        if (success) {
            String accessToken = jwtUtil.generateAccessToken(data.getUserid());
            String refreshToken = jwtUtil.generateRefreshToken(data.getUserid());

            // ✅ 기존 토큰 삭제 및 새 토큰 저장
            tokenService.deleteTokenByUserid(data.getUserid());
            tokenService.saveRefreshToken(data.getUserid(), refreshToken);

            // ✅ 쿠키 설정
            cookieUtil.addCookies(response, accessToken, refreshToken);

            return ResponseEntity.ok(true);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
    }
}
