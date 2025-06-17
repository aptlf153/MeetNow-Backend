package com.example.meetnow.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import com.example.meetnow.util.JwtUtil;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
public class ApplyMeetController {

	//커밋조정
	
    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostMapping("/api/applications")
    public String applyToMeet(@RequestBody Map<String, String> data, HttpServletRequest request) {
        String refreshToken = extractRefreshToken(request);
        if (refreshToken == null) return "로그인이 필요합니다.";

        String userid = jwtUtil.extractUsername(refreshToken);
        int meetingId = Integer.parseInt(data.get("meetingId"));

        String checkSql = "SELECT COUNT(*) FROM application WHERE userid = ? AND meet_id = ?";
        Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, userid, meetingId);
        if (count != null && count > 0) return "이미 신청한 모임입니다.";

        String insertSql = "INSERT INTO application (userid, meet_id, accepted, applied_at) VALUES (?, ?, false, NOW())";
        jdbcTemplate.update(insertSql, userid, meetingId);

        return "신청 완료!";
    }

    
    @GetMapping("/api/applications/meeting/{meetingId}")
    public List<Map<String, Object>> getApplicants(@PathVariable int meetingId) {
        String sql = "SELECT a.userid, a.accepted, u.nickname " +
                     "FROM application a " +
                     "JOIN user u ON a.userid = u.userid " +
                     "WHERE a.meet_id = ? " +
                     "ORDER BY a.applied_at ASC";

        return jdbcTemplate.query(sql, (rs, rowNum) -> Map.of(
            "userid", rs.getString("userid"),
            "accepted", rs.getBoolean("accepted"),
            "nickname", rs.getString("nickname")
        ), meetingId);
    }


    @PutMapping("/api/applications/accept")
    public String acceptApplicant(@RequestBody Map<String, String> data) {
        String userid = data.get("userid");
        int meetingId = Integer.parseInt(data.get("meetingId"));

        String sql = "UPDATE application SET accepted = true WHERE userid = ? AND meet_id = ?";
        jdbcTemplate.update(sql, userid, meetingId);

        return "신청 수락 완료";
    }

    @DeleteMapping("/api/applications/kick")
    public String kickApplicant(@RequestBody Map<String, String> data) {
        String userid = data.get("userid");
        int meetingId = Integer.parseInt(data.get("meetingId"));

        String sql = "DELETE FROM application WHERE userid = ? AND meet_id = ?";
        jdbcTemplate.update(sql, userid, meetingId);

        return "신청자 강퇴 완료";
    }

    @GetMapping("/api/user/info")
    public Map<String, String> getCurrentUser(HttpServletRequest request) {
        String refreshToken = extractRefreshToken(request);
        if (refreshToken == null) return Map.of("userid", "");

        String userid = jwtUtil.extractUsername(refreshToken);
        return Map.of("userid", userid);
    }

    //댓글 모두 불러오기
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

    
    @GetMapping("/api/applications/check")
    public Map<String, Boolean> checkAlreadyApplied(@RequestParam int meetingId, HttpServletRequest request) {
        String refreshToken = extractRefreshToken(request);
        if (refreshToken == null) return Map.of("alreadyApplied", false, "alreadyAccepted", false);

        String userid = jwtUtil.extractUsername(refreshToken);
        if (userid == null) return Map.of("alreadyApplied", false, "alreadyAccepted", false);

        String sql = "SELECT accepted FROM application WHERE userid = ? AND meet_id = ?";
        List<Boolean> result = jdbcTemplate.query(sql, (rs, rowNum) -> rs.getBoolean("accepted"), userid, meetingId);

        if (result.isEmpty()) return Map.of("alreadyApplied", false, "alreadyAccepted", false);
        else return Map.of("alreadyApplied", true, "alreadyAccepted", result.get(0));
    }

    
    private String extractRefreshToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("refreshToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
    
    @DeleteMapping("/api/applications/delect")
    public ResponseEntity<?> leaveMeeting(@RequestBody Map<String, Object> data, HttpServletRequest request) {
    	
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

        try {
            String userid = jwtUtil.extractUsername(refreshToken);
            int meetingId = (int) data.get("meetingId");

            String sql = "DELETE FROM application WHERE userid = ? AND meet_id = ?";
            int result = jdbcTemplate.update(sql, userid, meetingId);
            
            String commentSql = "DELETE FROM comment WHERE userid = ? AND meeting_id = ?";
            int commentResult = jdbcTemplate.update(commentSql, userid, meetingId);

            if (result == 0) return ResponseEntity.status(404).body("신청 기록이 없습니다.");
            if (commentResult == 0) {
                return ResponseEntity.status(404).body("삭제할 댓글이 없습니다.");
            }
            
            return ResponseEntity.ok("모임에서 성공적으로 나갔습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(400).body("요청 처리 중 오류가 발생했습니다.");
        }
    }

	private String getTokenFromCookies(HttpServletRequest request) {
		// TODO Auto-generated method stub
		return null;
	}

} 
