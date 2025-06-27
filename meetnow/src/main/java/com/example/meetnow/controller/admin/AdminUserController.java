package com.example.meetnow.controller.admin;

import com.example.meetnow.Entity.AdminEt;
import com.example.meetnow.Entity.User;
import com.example.meetnow.repository.user.UsersRepository;
import com.example.meetnow.service.auth.AuthService;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final UsersRepository usersRepository;
    private final AuthService authService;

    public AdminUserController(UsersRepository usersRepository, AuthService authService) {
        this.usersRepository = usersRepository;
        this.authService = authService;
    }

    @GetMapping
    public ResponseEntity<?> getAllUsers(HttpServletRequest request) {
        try {
            authService.authenticateAdmin(request); // ✅ 관리자 인증
            return ResponseEntity.ok(usersRepository.findAll());
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchUsers(@RequestParam String keyword, HttpServletRequest request) {
        try {
            authService.authenticateAdmin(request); // ✅ 관리자 인증
            return ResponseEntity.ok(usersRepository.searchUsersByUseridOrEmail(keyword));
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable int id, HttpServletRequest request) {
        try {
            authService.authenticateAdmin(request); // ✅ 관리자 인증
            Optional<User> optionalUser = usersRepository.findById(id);
            if (optionalUser.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            usersRepository.delete(optionalUser.get());
            return ResponseEntity.ok("삭제 완료");
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }
}
