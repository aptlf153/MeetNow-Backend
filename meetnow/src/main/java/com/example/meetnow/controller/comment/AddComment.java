package com.example.meetnow.controller.comment;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import com.example.meetnow.util.jwt.JwtUtil;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class AddComment {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/api/comments")
    public ResponseEntity<?> addComment(@RequestBody Map<String, String> data, HttpServletRequest request) {
        String refreshToken = getTokenFromCookies(request);
        if (refreshToken == null || refreshToken.isBlank()) {
            return ResponseEntity.status(401).body("로그인이 필요합니다.");
        }

        String userid = jwtUtil.extractUsername(refreshToken);
        if (userid == null || userid.isBlank()) {
            return ResponseEntity.status(401).body("유효하지 않은 토큰입니다.");
        }

        String content = data.get("content");
        int meetingId = Integer.parseInt(data.get("meetingId"));

        String sql = "INSERT INTO comment (userid, content, meeting_id) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, userid, content, meetingId);

        return ResponseEntity.ok(Map.of(
            "userid", userid,
            "content", content,
            "createdAt", LocalDateTime.now().toString()
        ));
    }

    private String getTokenFromCookies(HttpServletRequest request) {
        if (request.getCookies() == null) return null;
        for (Cookie cookie : request.getCookies()) {
            if ("refreshToken".equals(cookie.getName())) return cookie.getValue();
        }
        return null;
    }
} 
