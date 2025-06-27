package com.example.meetnow.controller.user;

import com.example.meetnow.service.auth.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class getCurrentUser {

    private final AuthService authService;

    public getCurrentUser(AuthService authService) {
        this.authService = authService;
    }

    // 단일 유저 아이디 조회 (절대 경로 사용)
    @GetMapping("/api/user/info")
    public Map<String, String> getCurrentUser(HttpServletRequest request) {
        String userid = authService.authenticateUser(request);
        return Map.of("userid", userid);
    }
}
