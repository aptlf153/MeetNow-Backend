package com.example.meetnow.controller.inquiry;

import com.example.meetnow.Entity.Inquiry;
import com.example.meetnow.repository.inquiry.InquiryRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin/inquiries")
@RequiredArgsConstructor
public class AdminInquiryController {

    private final InquiryRepository inquiryRepository;

    // 전체 문의 목록 조회
    @GetMapping
    public List<Inquiry> getAllInquiries() {
        return inquiryRepository.findAll(); // 또는 createdAt 기준 정렬 추가 가능
    }

    // 답변 등록 (isAnswered = true 로 전환)
    @PutMapping("/{id}/answer")
    public ResponseEntity<?> submitAnswer(@PathVariable Integer id, @RequestBody String answer) {
        Optional<Inquiry> optional = inquiryRepository.findById(id);
        if (optional.isEmpty()) return ResponseEntity.notFound().build();

        Inquiry inquiry = optional.get();
        inquiry.setAnswer(answer);
        inquiry.setAnswered(true);

        return ResponseEntity.ok(inquiryRepository.save(inquiry));
    }
}
