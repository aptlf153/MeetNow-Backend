package com.example.meetnow.controller.comment;

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
public class CommentController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private JwtUtil jwtUtil;

    @DeleteMapping("/api/comments/{commentId}")
    public ResponseEntity<?> deleteComment(@PathVariable int commentId, HttpServletRequest request) {
        String userid = getCurrentUser(request);
        if (userid == null) {
            return ResponseEntity.status(401).body("로그인이 필요합니다.");
        }

        // 댓글 작성자와 모임 주최자 확인
        String query = "SELECT c.userid AS comment_user, m.userid AS host_user " +
                       "FROM comment c JOIN meet m ON c.meeting_id = m.id WHERE c.id = ?";
        Map<String, Object> result = jdbcTemplate.queryForMap(query, commentId);

        String commentUser = (String) result.get("comment_user");
        String hostUser = (String) result.get("host_user");

        if (!userid.equals(commentUser) && !userid.equals(hostUser)) {
            return ResponseEntity.status(403).body("삭제 권한이 없습니다.");
        }

        String deleteSql = "DELETE FROM comment WHERE id = ?";
        jdbcTemplate.update(deleteSql, commentId);

        return ResponseEntity.ok("댓글 삭제 완료");
    }

    private String getCurrentUser(HttpServletRequest request) {
        if (request.getCookies() == null) return null;
        for (Cookie cookie : request.getCookies()) {
            if ("refreshToken".equals(cookie.getName())) {
                return jwtUtil.extractUsername(cookie.getValue());
            }
        }
        return null;
    }
    
    @GetMapping("/api/applications/count")
    public Map<String, Integer> getApplicationCount(@RequestParam int meetingId) {
        String sql = "SELECT COUNT(*) FROM application WHERE meet_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, meetingId);
        return Map.of("count", count != null ? count : 0);
    }
}
