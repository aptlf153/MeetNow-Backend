package com.example.meetnow.service.auth;

import com.example.meetnow.Entity.AdminEt;
import com.example.meetnow.Entity.RefreshToken;
import com.example.meetnow.repository.admin.AdminRepository;
import com.example.meetnow.repository.user.UserRepository;
import com.example.meetnow.util.jwt.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
public class AuthService {

    private final JwtUtil jwtUtil;
    private final AdminRepository adminRepository;
    private final UserRepository userRepository;
    private final TokenService tokenService;

    public AuthService(JwtUtil jwtUtil, AdminRepository adminRepository, UserRepository userRepository, TokenService tokenService) {
        this.jwtUtil = jwtUtil;
        this.adminRepository = adminRepository;
        this.userRepository = userRepository;
        this.tokenService = tokenService;
    }

    /**
     * 일반 사용자 인증 (refreshToken 존재 + DB 토큰 일치 + 유저 존재 여부 확인)
     */
    public String authenticateUser(HttpServletRequest request) {
        String refreshToken = extractRefreshToken(request);
        String userId = jwtUtil.extractUsername(refreshToken);

        // 토큰 일치 확인
        RefreshToken savedToken = tokenService.findTokensByUserid(userId)
                .orElseThrow(() -> new RuntimeException("저장된 토큰이 없습니다."));

        if (!refreshToken.equals(savedToken.getRefreshtoken())) {
            throw new RuntimeException("유효하지 않은 토큰입니다.");
        }

        // 유저 DB 확인
        userRepository.findByUserid(userId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자입니다."));

        return userId;
    }

    /**
     * 관리자 인증 (refreshToken 존재 + DB 토큰 일치 + 관리자 DB 존재 여부 확인)
     */
    public AdminEt authenticateAdmin(HttpServletRequest request) {
        String refreshToken = extractRefreshToken(request);
        String userId = jwtUtil.extractUsername(refreshToken);

        // 토큰 일치 확인
        RefreshToken savedToken = tokenService.findTokensByUserid(userId)
                .orElseThrow(() -> new RuntimeException("저장된 토큰이 없습니다."));

        if (!refreshToken.equals(savedToken.getRefreshtoken())) {
            throw new RuntimeException("유효하지 않은 토큰입니다.");
        }

        // 관리자 DB 확인
        return adminRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("관리자 권한이 없습니다."));
    }

    /**
     * 쿠키에서 refreshToken 추출
     */
    private String extractRefreshToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            throw new RuntimeException("로그인이 필요합니다.");
        }

        return Arrays.stream(cookies)
                .filter(cookie -> "refreshToken".equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("로그인이 필요합니다."));
    }
}
