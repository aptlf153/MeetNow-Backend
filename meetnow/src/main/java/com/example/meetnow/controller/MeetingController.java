package com.example.meetnow.controller;

import jakarta.persistence.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.*;

import com.example.meetnow.util.JwtUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;


@Entity
@Table(name = "meet")
class Meeting {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userid;
    private String title;
    private String description;
    private Double latitude;
    private Double longitude;
    private String location;
    private LocalDate meetDate;

    // 새로운 imageUrl 필드 추가
    private String imageUrl;

    // Getter / Setter 추가
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUserid() { return userid; }
    public void setUserid(String userid) { this.userid = userid; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public LocalDate getMeetDate() { return meetDate; }
    public void setMeetDate(LocalDate meetDate) { this.meetDate = meetDate; }

    // imageUrl의 getter와 setter 추가
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}

// ✅ Repository
interface MeetingRepository extends org.springframework.data.jpa.repository.JpaRepository<Meeting, Long> {
	List<Meeting> findByUserid(String userid);
	List<Meeting> findByTitleContainingOrDescriptionContainingOrLocationContaining(String title, String description, String location);
}

// ✅ Controller
@RestController
@RequestMapping("/api/meetings")
class MeetingController {
	
    @Autowired
    private JdbcTemplate jdbcTemplate;
	
    @Autowired
    private JwtUtil jwtUtil; // JWT 유틸리티 클래스	

    @Autowired
    private MeetingRepository meetingRepository;

    // 전체 모임 리스트
    @GetMapping
    public List<Meeting> getAllMeetings() {
    	//모든 모임 데이터를 가져오는 API 엔드포인트
        return meetingRepository.findAll();
    }

    // ✅ 특정 모임 상세 조회
    @GetMapping("/{id}")
    public ResponseEntity<Meeting> getMeetingById(@PathVariable Long id) {
        return meetingRepository.findById(id)
                .map(meeting -> ResponseEntity.ok().body(meeting))
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/search")
    public ResponseEntity<?> searchMeetings(@RequestParam String keyword) {
        List<Meeting> meetings = meetingRepository
            .findByTitleContainingOrDescriptionContainingOrLocationContaining(keyword, keyword, keyword);
        return ResponseEntity.ok(meetings);
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
    
}
