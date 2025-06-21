package com.example.meetnow.controller.meeting;

import jakarta.persistence.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.*;

import com.example.meetnow.Entity.Meeting;
import com.example.meetnow.repository.meeting.MeetingRepository;
import com.example.meetnow.util.jwt.JwtUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.data.jpa.repository.Query;

// ✅ Controller
@RestController
@RequestMapping("/api/meetings")
public class MeetingController {
	
    @Autowired
    private JdbcTemplate jdbcTemplate;
	 
    @Autowired
    private JwtUtil jwtUtil; // JWT 유틸리티 클래스	

    @Autowired
    private MeetingRepository meetingRepository;

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

        return jdbcTemplate.query(sql, (rs, rowNum) -> Map.of(
            "id", rs.getInt("id"),
            "title", rs.getString("title"),
            "description", rs.getString("description"),
            "location", rs.getString("location"),
            "userid", rs.getString("userid"),
            "meetDate", rs.getDate("meet_date").toLocalDate(),
            "imageUrl", rs.getString("image_url"),
            "nickname", rs.getString("nickname")
        ));
    }

    // ✅모임 상세 조회
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

        List<Map<String, Object>> result = jdbcTemplate.query(sql, (rs, rowNum) -> Map.of(
            "id", rs.getInt("id"),
            "title", rs.getString("title"),
            "description", rs.getString("description"),
            "location", rs.getString("location"),
            "userid", rs.getString("userid"),
            "meetDate", rs.getDate("meet_date").toLocalDate(),
            "imageUrl", rs.getString("image_url"),
            "nickname", rs.getString("nickname")
        ), like, like, like, like);

        return ResponseEntity.ok(result);
    }
    
    // ✅ [중요] 내가 만든 모임 리스트 (JWT 토큰에서 ID 추출)
    @GetMapping("/my")
    public ResponseEntity<?> getUserMeetings(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        String refreshToken = null;

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("refreshToken".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                }
            }
        }

        if (refreshToken == null) {
            return ResponseEntity.status(401).body("로그인이 필요합니다.");
        }

        try {
            String userid = jwtUtil.extractUsername(refreshToken);
            List<Meeting> myMeetings = meetingRepository.findByUserid(userid);
            return ResponseEntity.ok(myMeetings);
        } catch (Exception e) {
            return ResponseEntity.status(401).body("유효하지 않은 토큰입니다.");
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> updateMeeting(@PathVariable Long id, @RequestBody Meeting updatedMeeting) {
        Optional<Meeting> optionalMeeting = meetingRepository.findById(id);

        if (optionalMeeting.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Meeting meeting = optionalMeeting.get();

        // 보안상 수정 권한 체크 필요 (예: userid 일치 여부 확인)
        if (!meeting.getUserid().equals(updatedMeeting.getUserid())) {
            return ResponseEntity.status(403).body("수정 권한이 없습니다.");
        }

        // 필요한 필드들 업데이트
        meeting.setTitle(updatedMeeting.getTitle());
        meeting.setDescription(updatedMeeting.getDescription());
        meeting.setLatitude(updatedMeeting.getLatitude());
        meeting.setLongitude(updatedMeeting.getLongitude());
        meeting.setLocation(updatedMeeting.getLocation());
        meeting.setMeetDate(updatedMeeting.getMeetDate());

        meetingRepository.save(meeting);

        return ResponseEntity.ok(meeting);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMeeting(@PathVariable Long id, HttpServletRequest request) {
        Optional<Meeting> optionalMeeting = meetingRepository.findById(id);
        if (optionalMeeting.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Meeting meeting = optionalMeeting.get();

        // 보안: 삭제 권한 체크 (예: JWT에서 userid 추출 후 비교)
        String useridFromToken = null;
        try {
            // 쿠키에서 토큰 추출 + userid 확인 (jwtUtil 활용)
            Cookie[] cookies = request.getCookies();
            String refreshToken = null;
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("refreshToken".equals(cookie.getName())) {
                        refreshToken = cookie.getValue();
                    }
                }
            }
            if (refreshToken == null) return ResponseEntity.status(401).body("로그인이 필요합니다.");
            useridFromToken = jwtUtil.extractUsername(refreshToken);
        } catch (Exception e) {
            return ResponseEntity.status(401).body("유효하지 않은 토큰입니다.");
        }
        
        if (!meeting.getUserid().equals(useridFromToken)) {
            return ResponseEntity.status(403).body("삭제 권한이 없습니다.");
        }
        
        meetingRepository.delete(meeting);
        return ResponseEntity.ok("삭제 완료");
    }

    
    //내가 신청한 모임 
    @GetMapping("/applied")
    public ResponseEntity<?> getAppliedMeetings(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        String refreshToken = null;

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("refreshToken".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                }
            }
        }

        if (refreshToken == null) {
            return ResponseEntity.status(401).body("로그인이 필요합니다.");
        }

        try {
            String userid = jwtUtil.extractUsername(refreshToken);

            String sql = """
                    SELECT m.id, m.title, m.description, m.meet_date, m.location, a.accepted
                    FROM application a
                    JOIN meet m ON a.meet_id = m.id
                    WHERE a.userid = ?
                    ORDER BY m.meet_date DESC
                """;

            List<Map<String, Object>> appliedMeetings = jdbcTemplate.query(sql, (rs, rowNum) -> Map.of(
                    "id", rs.getInt("id"),
                    "title", rs.getString("title"),
                    "description", rs.getString("description"),
                    "meetDate", rs.getString("meet_date"),
                    "location", rs.getString("location"),
                    "accepted", rs.getBoolean("accepted")
                ), userid);
            
            System.out.println(appliedMeetings);

            return ResponseEntity.ok(appliedMeetings);
        } catch (Exception e) {
            return ResponseEntity.status(401).body("유효하지 않은 토큰입니다.");
        }
    }
    
    @PutMapping("/{id}/close")
    public ResponseEntity<?> closeMeeting(@PathVariable Long id, HttpServletRequest request) {
        // 1. 쿠키에서 refreshToken 추출
        String refreshToken = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("refreshToken".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                }
            }
        }

        if (refreshToken == null) {
            return ResponseEntity.status(401).body("로그인이 필요합니다.");
        }
        
        // 2. JWT에서 userid 추출
        String userid;
        try {
            userid = jwtUtil.extractUsername(refreshToken);
        } catch (Exception e) {
            return ResponseEntity.status(401).body("유효하지 않은 토큰입니다.");
        }
        
        // 3. 모임 조회 및 권한 확인
        Optional<Meeting> optionalMeeting = meetingRepository.findById(id);
        if (optionalMeeting.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Meeting meeting = optionalMeeting.get();
        if (!meeting.getUserid().equals(userid)) {
            return ResponseEntity.status(403).body("마감 권한이 없습니다.");
        }

        // 4. 마감 처리
        System.out.println(meeting);
        meeting.setClosed(true); // closed 필드가 boolean이고, is_closed 컬럼에 매핑되어 있어야 함
        meetingRepository.save(meeting);

        return ResponseEntity.ok("모임이 마감되었습니다.");
    }

}
