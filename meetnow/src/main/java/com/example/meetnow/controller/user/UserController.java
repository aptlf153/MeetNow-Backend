package com.example.meetnow.controller.user;

import java.util.Map;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.meetnow.Entity.User;
import com.example.meetnow.repository.user.UserRepository;
import com.example.meetnow.service.auth.AuthService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final AuthService authService;

    /**
     * ✅ 간단한 인증 확인 (userId만 리턴)
     */
    @GetMapping("/id")
    public Map<String, String> getCurrentUserId(HttpServletRequest request) {
        String userId = authService.authenticateUser(request);  // 인증 포함
        return Map.of("userid", userId);
    }

    /**
     * ✅ 내 전체 정보 조회
     */
    @GetMapping("/me")
    public ResponseEntity<?> getMyInfo(HttpServletRequest request) {
        String userId = authService.authenticateUser(request);

        Optional<User> optionalUser = userRepository.findByUserid(userId);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(404).body("유저를 찾을 수 없습니다.");
        }

        return ResponseEntity.ok(optionalUser.get());
    }

    /**
     * ✅ 닉네임 + 전화번호 수정
     */
    @PutMapping("/update-profile")
    public ResponseEntity<?> updateProfile(@RequestBody User updatedUser, HttpServletRequest request) {
        String userId = authService.authenticateUser(request);

        Optional<User> optionalUser = userRepository.findByUserid(userId);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(404).body("유저를 찾을 수 없습니다.");
        }

        User user = optionalUser.get();

        // 닉네임 중복 검사 및 수정
        if (updatedUser.getNickname() != null &&
                !updatedUser.getNickname().equals(user.getNickname())) {

            boolean nicknameExists = userRepository.existsByNickname(updatedUser.getNickname());
            if (nicknameExists) {
                return ResponseEntity.status(409).body("이미 사용 중인 닉네임입니다.");
            }

            user.setNickname(updatedUser.getNickname());
        }

        // 전화번호 수정
        if (updatedUser.getPhone() != null) {
            user.setPhone(updatedUser.getPhone());
        }

        userRepository.save(user);
        return ResponseEntity.ok("수정 완료");
    }
    

}
