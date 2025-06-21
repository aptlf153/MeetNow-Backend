package com.example.meetnow.controller.comment;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.meetnow.service.auth.AuthService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
public class AddComment {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private AuthService authService; // ✅ 인증 전담 서비스

    @PostMapping("/api/comments")
    public ResponseEntity<?> addComment(@RequestBody Map<String, String> data, HttpServletRequest request) {
        String userid;

        try {
        	userid = authService.authenticateUser(request);
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }

        String content = data.get("content");
        int meetingId;

        try {
            meetingId = Integer.parseInt(data.get("meetingId"));
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body("meetingId는 숫자여야 합니다.");
        }

        String sql = "INSERT INTO comment (userid, content, meeting_id) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, userid, content, meetingId);

        return ResponseEntity.ok(Map.of(
                "userid", userid,
                "content", content,
                "createdAt", LocalDateTime.now().toString()
        ));
    }
}
