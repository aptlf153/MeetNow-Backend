package com.example.meetnow.controller.notice;

import com.example.meetnow.Entity.AdminEt;
import com.example.meetnow.Entity.Notice;
import com.example.meetnow.repository.notice.NoticeRepository;
import com.example.meetnow.service.auth.AuthService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin/notices")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeRepository noticeRepository;
    private final AuthService authService;

    // 공지 목록 조회 (모두 접근 가능)
    @GetMapping
    public List<Notice> getAllNotices() {
        return noticeRepository.findAll();
    }

    // 공지 등록 (관리자만)
    @PostMapping
    public ResponseEntity<?> createNotice(@RequestBody Notice notice, HttpServletRequest request) {
        try {
            AdminEt admin = authService.authenticateAdmin(request); // 관리자 인증

            if (notice.getTitle() == null || notice.getContent() == null) {
                return ResponseEntity.badRequest().body("제목과 내용을 입력해주세요.");
            }

            return ResponseEntity.ok(noticeRepository.save(notice));

        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }

    // 공지 수정 (관리자만)
    @PutMapping("/{id}")
    public ResponseEntity<?> updateNotice(@PathVariable Integer id, @RequestBody Notice updated, HttpServletRequest request) {
        try {
            authService.authenticateAdmin(request); // 관리자 인증

            Optional<Notice> optional = noticeRepository.findById(id);
            if (optional.isEmpty()) return ResponseEntity.notFound().build();

            Notice existing = optional.get();
            existing.setTitle(updated.getTitle());
            existing.setContent(updated.getContent());

            return ResponseEntity.ok(noticeRepository.save(existing));
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }

    // 공지 삭제 (관리자만)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteNotice(@PathVariable Integer id, HttpServletRequest request) {
        try {
            authService.authenticateAdmin(request); // 관리자 인증

            if (!noticeRepository.existsById(id)) {
                return ResponseEntity.notFound().build();
            }

            noticeRepository.deleteById(id);
            return ResponseEntity.ok("삭제 완료");

        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }
}
