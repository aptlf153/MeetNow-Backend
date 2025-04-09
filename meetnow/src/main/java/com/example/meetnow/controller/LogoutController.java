package com.example.meetnow.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.meetnow.Entity.RefreshToken;
import com.example.meetnow.dto.DTO;
import com.example.meetnow.service.TokenService;
import com.example.meetnow.util.JwtUtil;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true") // React 앱의 URL
public class LogoutController {
	
	@Autowired
	private TokenService tokenService;
	
	@Autowired
	private JwtUtil jwtUtil;
	
	 @PostMapping("/api/users/logout")
	 public void deleteTokenByUserid(HttpServletResponse response, HttpServletRequest request) {
		 
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
		 
		    Optional<RefreshToken> tokenOptional = tokenService.findTokensByUserid(userid);
		    
		    tokenOptional.ifPresent(token -> tokenService.deleteRefreshToken(token));
		    
            ResponseCookie accessCookie = ResponseCookie.from("accessToken", null)
                    .path("/")
                    .maxAge(0)
                    .sameSite("Lax")
                    .build();
            response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
            
            ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", null)
                    .path("/")
                    .maxAge(0)
                    .sameSite("Lax")
                    .build();
            response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());           
		}
}
