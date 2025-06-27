package com.example.meetnow.controller.admin;

import com.example.meetnow.Entity.AdminEt;
import com.example.meetnow.Entity.Meeting;
import com.example.meetnow.repository.meeting.MeetingRepository;
import com.example.meetnow.service.auth.AuthService;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/admin/meetings")
public class AdminMeetingController {

    private final MeetingRepository meetingRepository;
    private final AuthService authService;

    public AdminMeetingController(MeetingRepository meetingRepository, AuthService authService) {
        this.meetingRepository = meetingRepository;
        this.authService = authService;
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMeeting(@PathVariable Long id, HttpServletRequest request) {
        try {
            AdminEt admin = authService.authenticateAdmin(request); // ✅ 관리자 인증

            Optional<Meeting> optionalMeeting = meetingRepository.findById(id);
            if (optionalMeeting.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            meetingRepository.delete(optionalMeeting.get());
            return ResponseEntity.ok("삭제 완료");

        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }
}
