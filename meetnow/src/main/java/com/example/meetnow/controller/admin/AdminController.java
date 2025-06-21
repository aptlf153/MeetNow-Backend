package com.example.meetnow.controller.admin;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.meetnow.Entity.AdminEt;
import com.example.meetnow.Entity.Meeting;
import com.example.meetnow.Entity.User;
import com.example.meetnow.repository.admin.AdminRepository;
import com.example.meetnow.repository.meeting.MeetingRepository;
import com.example.meetnow.util.jwt.JwtUtil;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Optional;

interface UsersRepository extends JpaRepository<User, Integer> {
    @Query("SELECT u FROM User u WHERE (u.userid LIKE %:keyword% OR u.email LIKE %:keyword% OR u.nickname LIKE %:keyword%)")
    List<User> searchUsersByUseridOrEmail(@Param("keyword") String keyword);
}

@RestController
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class AdminController {

    private final MeetingRepository meetingRepository;
    private final JwtUtil jwtUtil;
    private final UsersRepository usersRepository;
    private final AdminRepository adminRepository;

    public AdminController(MeetingRepository meetingRepository, JwtUtil jwtUtil,
                           UsersRepository usersRepository, AdminRepository adminRepository) {
        this.meetingRepository = meetingRepository;
        this.jwtUtil = jwtUtil;
        this.usersRepository = usersRepository;
        this.adminRepository = adminRepository;
    }

    @GetMapping("/api/userList")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(usersRepository.findAll());
    }

    @GetMapping("/api/userList/search")
    public ResponseEntity<List<User>> getAllUsersSearch(@RequestParam String keyword) {
        return ResponseEntity.ok(usersRepository.searchUsersByUseridOrEmail(keyword));
    }

    @DeleteMapping("/api/admin/meetings/{id}")
    public ResponseEntity<?> deleteMeeting(@PathVariable Long id, HttpServletRequest request) {
        Optional<Meeting> optionalMeeting = meetingRepository.findById(id);
        if (optionalMeeting.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        String useridFromToken = null;
        AdminEt adminet = null;

        try {
            Cookie[] cookies = request.getCookies();
            String refreshToken = null;
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("refreshToken".equals(cookie.getName())) {
                        refreshToken = cookie.getValue();
                        break;
                    }
                }
            }

            if (refreshToken == null) {
                return ResponseEntity.status(401).body("로그인이 필요합니다.");
            }

            useridFromToken = jwtUtil.extractUsername(refreshToken);
            Optional<AdminEt> optionalAdmin = adminRepository.findById(useridFromToken);
            if (optionalAdmin.isEmpty()) {
                return ResponseEntity.status(403).body("관리자 권한이 없습니다.");
            }

            adminet = optionalAdmin.get();

        } catch (Exception e) {
            return ResponseEntity.status(401).body("유효하지 않은 토큰입니다.");
        }

        if (!adminet.getId().equals(useridFromToken)) {
            return ResponseEntity.status(403).body("삭제 권한이 없습니다.");
        }

        meetingRepository.delete(optionalMeeting.get());
        return ResponseEntity.ok("삭제 완료");
    }
    
    
    //어드민 권한 아이디 삭제
    @DeleteMapping("/api/admin/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable int id, HttpServletRequest request) {
        Optional<User> optionalUser = usersRepository.findById(id);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        String useridFromToken = null;
        AdminEt adminet = null;

        try {
            Cookie[] cookies = request.getCookies();
            String refreshToken = null;
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("refreshToken".equals(cookie.getName())) {
                        refreshToken = cookie.getValue();
                        break;
                    }
                }
            }

            if (refreshToken == null) {
                return ResponseEntity.status(401).body("로그인이 필요합니다.");
            }

            useridFromToken = jwtUtil.extractUsername(refreshToken);
            Optional<AdminEt> optionalAdmin = adminRepository.findById(useridFromToken);
            if (optionalAdmin.isEmpty()) {
                return ResponseEntity.status(403).body("관리자 권한이 없습니다.");
            }

            adminet = optionalAdmin.get();

        } catch (Exception e) {
            return ResponseEntity.status(401).body("유효하지 않은 토큰입니다.");
        }

        if (!adminet.getId().equals(useridFromToken)) {
            return ResponseEntity.status(403).body("삭제 권한이 없습니다.");
        }

        usersRepository.delete(optionalUser.get());
        return ResponseEntity.ok("삭제 완료");
    }    
}
