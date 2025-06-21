package com.example.meetnow.controller.comment;

import com.example.meetnow.service.auth.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class CommentController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private AuthService authService; // ✅ 리팩토링: JwtUtil → AuthService

    /**
     * 댓글 삭제 (댓글 작성자 또는 모임 주최자만 가능)
     */
    @DeleteMapping("/api/comments/{commentId}")
    public ResponseEntity<?> deleteComment(@PathVariable int commentId, HttpServletRequest request) {
        String userid;
        try {
            userid = authService.authenticateUser(request); // ✅ 공통 인증 로직 사용
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }

        try {
            String query = "SELECT c.userid AS comment_user, m.userid AS host_user " +
                           "FROM comment c JOIN meet m ON c.meeting_id = m.id WHERE c.id = ?";
            Map<String, Object> result = jdbcTemplate.queryForMap(query, commentId);

            String commentUser = (String) result.get("comment_user");
            String hostUser = (String) result.get("host_user");

            if (!userid.equals(commentUser) && !userid.equals(hostUser)) {
                return ResponseEntity.status(403).body("삭제 권한이 없습니다.");
            }

            jdbcTemplate.update("DELETE FROM comment WHERE id = ?", commentId);
            return ResponseEntity.ok("댓글 삭제 완료");

        } catch (Exception e) {
            return ResponseEntity.status(404).body("댓글을 찾을 수 없습니다.");
        }
    }
    
    // 특정 모임의 댓글 목록 조회
    @GetMapping("/api/comments/meeting/{meetingId}")
    public List<Map<String, Object>> getComments(@PathVariable int meetingId) {
        String sql = """
            SELECT c.id, c.userid, c.content, c.created_at, u.nickname
            FROM comment c
            INNER JOIN user u ON c.userid = u.userid
            WHERE c.meeting_id = ?
            ORDER BY c.created_at DESC
        """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> Map.of(
                "id", rs.getInt("id"),
                "userid", rs.getString("userid"),
                "nickname", rs.getString("nickname"),
                "content", rs.getString("content"),
                "createdAt", rs.getTimestamp("created_at").toString()
        ), meetingId);
    }    
}
