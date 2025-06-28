package com.example.meetnow.controller.meeting;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import com.example.meetnow.Entity.Meeting;
import com.example.meetnow.repository.meeting.MeetingRepository;
import com.example.meetnow.service.auth.AuthService;
import com.example.meetnow.util.jwt.JwtUtil;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/meetings")
public class MeetingController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private MeetingRepository meetingRepository;

    @Autowired
    private AuthService authService;

    // 전체 모임 리스트
    @GetMapping
    public List<Map<String, Object>> getAllMeetings() {
        String sql = """
            SELECT m.id, m.title, m.description, m.location, m.userid,
                   m.meet_date, m.image_url, u.nickname
            FROM meet m
            JOIN user u ON m.userid = u.userid
            WHERE m.closed = false
            ORDER BY m.meet_date DESC
        """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> Map.ofEntries(
            Map.entry("id", rs.getInt("id")),
            Map.entry("title", rs.getString("title")),
            Map.entry("description", rs.getString("description")),
            Map.entry("location", rs.getString("location")),
            Map.entry("userid", rs.getString("userid")),
            Map.entry("meetDate", rs.getDate("meet_date").toLocalDate()),
            Map.entry("imageUrl", rs.getString("image_url")),
            Map.entry("nickname", rs.getString("nickname"))
        ));
    }

    // 모임 상세 조회
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getMeetingById(@PathVariable int id) {
        String sql = """
            SELECT m.id, m.title, m.description, m.location, m.userid,
                   m.meet_date, m.image_url, m.closed, m.latitude, m.longitude, u.nickname
            FROM meet m
            JOIN user u ON m.userid = u.userid
            WHERE m.id = ?
        """;

        List<Map<String, Object>> result = jdbcTemplate.query(sql, (rs, rowNum) -> Map.ofEntries(
            Map.entry("id", rs.getInt("id")),
            Map.entry("title", rs.getString("title")),
            Map.entry("description", rs.getString("description")),
            Map.entry("location", rs.getString("location")),
            Map.entry("userid", rs.getString("userid")),
            Map.entry("meetDate", rs.getDate("meet_date").toLocalDate()),
            Map.entry("imageUrl", rs.getString("image_url")),
            Map.entry("closed", rs.getBoolean("closed")),
            Map.entry("latitude", rs.getDouble("latitude")),
            Map.entry("longitude", rs.getDouble("longitude")),
            Map.entry("nickname", rs.getString("nickname"))
        ), id);

        if (result.isEmpty()) {
            return ResponseEntity.notFound().build();
        } else {
            return ResponseEntity.ok(result.get(0));
        }
    }

    // 모임 검색
    @GetMapping("/search")
    public ResponseEntity<?> searchMeetings(@RequestParam String keyword) {
        String sql = """
            SELECT m.id, m.title, m.description, m.location, m.userid,
                   m.meet_date, m.image_url, u.nickname
            FROM meet m
            JOIN user u ON m.userid = u.userid
            WHERE (m.title LIKE ? OR m.description LIKE ? OR m.location LIKE ? OR u.nickname LIKE ?)
              AND m.closed = false
            ORDER BY m.meet_date DESC
        """;

        String like = "%" + keyword + "%";

        List<Map<String, Object>> result = jdbcTemplate.query(sql, (rs, rowNum) -> Map.ofEntries(
            Map.entry("id", rs.getInt("id")),
            Map.entry("title", rs.getString("title")),
            Map.entry("description", rs.getString("description")),
            Map.entry("location", rs.getString("location")),
            Map.entry("userid", rs.getString("userid")),
            Map.entry("meetDate", rs.getDate("meet_date").toLocalDate()),
            Map.entry("imageUrl", rs.getString("image_url")),
            Map.entry("nickname", rs.getString("nickname"))
        ), like, like, like, like);

        return ResponseEntity.ok(result);
    }

    // 내가 만든 모임
    @GetMapping("/my")
    public ResponseEntity<?> getMyMeetings(HttpServletRequest request) {
        try {
        	String userid = authService.authenticateUser(request);
            return ResponseEntity.ok(meetingRepository.findByUserid(userid));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }

    // 내가 신청한 모임
    @GetMapping("/applied")
    public ResponseEntity<?> getMyAppliedMeetings(HttpServletRequest request) {
        try {
        	String userid = authService.authenticateUser(request);

            String sql = """
                SELECT m.id, m.title, m.description, m.meet_date, m.location, a.accepted
                FROM application a
                JOIN meet m ON a.meet_id = m.id
                WHERE a.userid = ?
                ORDER BY m.meet_date DESC
            """;

            List<Map<String, Object>> result = jdbcTemplate.query(sql, (rs, rowNum) -> Map.ofEntries(
                Map.entry("id", rs.getInt("id")),
                Map.entry("title", rs.getString("title")),
                Map.entry("description", rs.getString("description")),
                Map.entry("meetDate", rs.getDate("meet_date").toLocalDate()),
                Map.entry("location", rs.getString("location")),
                Map.entry("accepted", rs.getBoolean("accepted"))
            ), userid);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }

    // 모임 수정
    @PutMapping("/{id}")
    public ResponseEntity<?> updateMeeting(@PathVariable Long id, @RequestBody Meeting updatedMeeting, HttpServletRequest request) {
        Optional<Meeting> optionalMeeting = meetingRepository.findById(id);
        if (optionalMeeting.isEmpty()) return ResponseEntity.notFound().build();

        try {
        	String userid = authService.authenticateUser(request);
            Meeting meeting = optionalMeeting.get();

            if (!meeting.getUserid().equals(userid)) {
                return ResponseEntity.status(403).body("수정 권한이 없습니다.");
            }

            meeting.setTitle(updatedMeeting.getTitle());
            meeting.setDescription(updatedMeeting.getDescription());
            meeting.setLatitude(updatedMeeting.getLatitude());
            meeting.setLongitude(updatedMeeting.getLongitude());
            meeting.setLocation(updatedMeeting.getLocation());
            meeting.setMeetDate(updatedMeeting.getMeetDate());
            meetingRepository.save(meeting);

            return ResponseEntity.ok(meeting);
        } catch (Exception e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }

    // 모임 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMeeting(@PathVariable Long id, HttpServletRequest request) {
        Optional<Meeting> optionalMeeting = meetingRepository.findById(id);
        if (optionalMeeting.isEmpty()) return ResponseEntity.notFound().build();

        try {
        	String userid = authService.authenticateUser(request);
            Meeting meeting = optionalMeeting.get();

            if (!meeting.getUserid().equals(userid)) {
                return ResponseEntity.status(403).body("삭제 권한이 없습니다.");
            }

            meetingRepository.delete(meeting);
            return ResponseEntity.ok("삭제 완료");
        } catch (Exception e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }

    // 모임 마감 처리
    @PutMapping("/{id}/close")
    public ResponseEntity<?> closeMeeting(@PathVariable Long id, HttpServletRequest request) {
        Optional<Meeting> optionalMeeting = meetingRepository.findById(id);
        if (optionalMeeting.isEmpty()) return ResponseEntity.notFound().build();

        try {
        	String userid = authService.authenticateUser(request);
            Meeting meeting = optionalMeeting.get();

            if (!meeting.getUserid().equals(userid)) {
                return ResponseEntity.status(403).body("마감 권한이 없습니다.");
            }

            meeting.setClosed(true);
            meetingRepository.save(meeting);
            return ResponseEntity.ok("모임이 마감되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }

	public void createMeeting(Meeting dummyMeetingRequest) {
		// TODO Auto-generated method stub
		
	}
}
