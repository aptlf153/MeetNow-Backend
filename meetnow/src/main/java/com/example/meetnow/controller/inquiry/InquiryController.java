package com.example.meetnow.controller.inquiry;

import com.example.meetnow.Entity.Inquiry;
import com.example.meetnow.repository.inquiry.InquiryRepository;
import com.example.meetnow.service.auth.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inquiries")
@RequiredArgsConstructor
public class InquiryController {

    private final InquiryRepository inquiryRepository;
    private final AuthService authService; // ✅ JwtUtil → AuthService로 대체

    // 1:1 문의 작성
    @PostMapping
    public ResponseEntity<?> createInquiry(@RequestBody Inquiry inquiry, HttpServletRequest request) {
        String userId;
        try {
            userId = authService.authenticateUser(request); // ✅ 인증 한 줄 처리
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }

        inquiry.setUserId(userId);
        inquiry.setAnswered(false);
        return ResponseEntity.ok(inquiryRepository.save(inquiry));
    }

    // 본인 문의 목록 보기
    @GetMapping("/me")
    public ResponseEntity<?> getMyInquiries(HttpServletRequest request) {
        String userId;
        try {
            userId = authService.authenticateUser(request); // ✅ 동일한 인증 방식
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }

        List<Inquiry> myList = inquiryRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return ResponseEntity.ok(myList);
    }
}
