package com.example.meetnow.controller.meeting;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.meetnow.service.auth.AuthService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

    @Autowired
    private AuthService authService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // 1. 모임 신청
    @PostMapping
    public String applyToMeet(@RequestBody Map<String, String> data, HttpServletRequest request) {
        String userid = authService.authenticateUser(request);
        int meetingId = Integer.parseInt(data.get("meetingId"));

        String checkSql = "SELECT COUNT(*) FROM application WHERE userid = ? AND meet_id = ?";
        Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, userid, meetingId);
        if (count != null && count > 0) return "이미 신청한 모임입니다.";

        String insertSql = "INSERT INTO application (userid, meet_id, accepted, applied_at) VALUES (?, ?, false, NOW())";
        jdbcTemplate.update(insertSql, userid, meetingId);

        return "신청 완료!";
    }

    // 2. 신청자 목록 조회
    @GetMapping("/meeting/{meetingId}")
    public List<Map<String, Object>> getApplicants(@PathVariable int meetingId) {
        String sql = """
            SELECT a.userid, a.accepted, u.nickname
            FROM application a
            JOIN user u ON a.userid = u.userid
            WHERE a.meet_id = ?
            ORDER BY a.applied_at ASC
        """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> Map.of(
                "userid", rs.getString("userid"),
                "accepted", rs.getBoolean("accepted"),
                "nickname", rs.getString("nickname")
        ), meetingId);
    }

    // 3. 신청 수락
    @PutMapping("/accept")
    public String acceptApplicant(@RequestBody Map<String, String> data) {
        String userid = data.get("userid");
        int meetingId = Integer.parseInt(data.get("meetingId"));

        String sql = "UPDATE application SET accepted = true WHERE userid = ? AND meet_id = ?";
        jdbcTemplate.update(sql, userid, meetingId);

        return "신청 수락 완료";
    }

    // 4. 신청자 강퇴
    @DeleteMapping("/kick")
    public String kickApplicant(@RequestBody Map<String, String> data) {
        String userid = data.get("userid");
        int meetingId = Integer.parseInt(data.get("meetingId"));

        String sql = "DELETE FROM application WHERE userid = ? AND meet_id = ?";
        jdbcTemplate.update(sql, userid, meetingId);

        return "신청자 강퇴 완료";
    }

    // 5. 승인 여부 확인
    @GetMapping("/check")
    public Map<String, Boolean> checkAlreadyApplied(@RequestParam int meetingId, HttpServletRequest request) {
        try {
            String userid = authService.authenticateUser(request);
            String sql = "SELECT accepted FROM application WHERE userid = ? AND meet_id = ?";
            List<Boolean> result = jdbcTemplate.query(sql, (rs, rowNum) -> rs.getBoolean("accepted"), userid, meetingId);

            if (result.isEmpty()) return Map.of("alreadyApplied", false, "alreadyAccepted", false);
            else return Map.of("alreadyApplied", true, "alreadyAccepted", result.get(0));
        } catch (Exception e) {
            return Map.of("alreadyApplied", false, "alreadyAccepted", false);
        }
    }

    // 6. 모임 나가기
    @DeleteMapping("/leave")
    public ResponseEntity<?> leaveMeeting(@RequestBody Map<String, Object> data, HttpServletRequest request) {
        try {
            String userid = authService.authenticateUser(request);
            int meetingId = (int) data.get("meetingId");

            int result = jdbcTemplate.update("DELETE FROM application WHERE userid = ? AND meet_id = ?", userid, meetingId);
            int commentResult = jdbcTemplate.update("DELETE FROM comment WHERE userid = ? AND meeting_id = ?", userid, meetingId);

            if (result == 0) return ResponseEntity.status(404).body("신청 기록이 없습니다.");
            if (commentResult == 0) return ResponseEntity.status(404).body("삭제할 댓글이 없습니다.");

            return ResponseEntity.ok("모임에서 성공적으로 나갔습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(400).body("요청 처리 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 모임 신청 수 조회
     */
    @GetMapping("/count")
    public Map<String, Integer> getApplicationCount(@RequestParam int meetingId) {
        String sql = "SELECT COUNT(*) FROM application WHERE meet_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, meetingId);
        return Map.of("count", count != null ? count : 0);
    }    
}
