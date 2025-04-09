package com.example.meetnow.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.stereotype.Controller;

import com.example.meetnow.Entity.RefreshToken;
import com.example.meetnow.dto.DTO;
import com.example.meetnow.response.JwtResponse;
import com.example.meetnow.service.TokenService;
import com.example.meetnow.util.JwtUtil;


import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Controller
@CrossOrigin("http://localhost:5173")
public class TokenController {
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private TokenService tokenService;

    // 토큰 검증 컨트롤
    @PostMapping("/api/token/check")
    public ResponseEntity<Boolean> tokenCheck(HttpServletRequest request) {

        // 쿠키 정보 출력
        Cookie[] cookies = request.getCookies();
        
        String accessToken = null;
        String refreshToken = null;

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("accessToken".equals(cookie.getName())) {
                    accessToken = cookie.getValue(); // accessToken 값 저장
                } else if ("refreshToken".equals(cookie.getName())) {
                    refreshToken = cookie.getValue(); // refreshToken 값 저장
                }
            }
        }
        
        String userid = jwtUtil.extractUsername(accessToken);
        
        boolean isValid = jwtUtil.validateToken(accessToken,userid);
        
        //ResponseEntity.ok(...): 성공적인 요청에 대한 200 OK 응답을 반환합니다.
        //ResponseEntity.badRequest(...): 잘못된 요청에 대한 400 Bad Request 응답을 반환합니다.
        //ResponseEntity.notFound(...): 요청한 리소스를 찾을 수 없는 경우 404 Not Found 응답을 반턴합니다.
        return ResponseEntity.ok(isValid);
    }
    
    
    @PostMapping("/api/token/refresh")
    public ResponseEntity<JwtResponse> getNewToken(HttpServletResponse response, HttpServletRequest request) {

        Cookie[] cookies = request.getCookies();
        
        String userIdToken = null;

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("refreshToken".equals(cookie.getName())) {
                	userIdToken = cookie.getValue(); // accessToken 값 저장
                }
            }
        }
        
        
        String userid = jwtUtil.extractUsername(userIdToken);
        
        // 유저 ID를 사용하여 토큰을 찾음
        Optional<RefreshToken> token = tokenService.findTokensByUserid(userid);
        
        // 토큰이 존재하면 RefreshToken의 문자열을 가져옴
        String gettoken = token.map(RefreshToken::getRefreshtoken)
                .orElse(null);
        
        //토큰 검증
        boolean isValid = jwtUtil.validateToken(gettoken, userid);
        
        if(isValid) 
        {
        	String accessToken = jwtUtil.generateAccessToken(userid);
        	
            ResponseCookie accessCookie = ResponseCookie.from("accessToken", accessToken)
                    .httpOnly(true) // 클라이언트에서 접근 불가
                    .path("/")
                    .maxAge(600)
                    .sameSite("Lax") // 같은 도메인에서만
                    .secure(false) // 개발 환경에서는 false로 설정
                    .domain("localhost") // 로컬 환경에서만 유효
                    .build();
            response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        	
            return ResponseEntity.ok(new JwtResponse(accessToken)); // JWT 반환
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null); // 인증 실패 시 401 반환
		}
    }

}
