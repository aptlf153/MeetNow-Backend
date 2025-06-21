package com.example.meetnow.repository.user;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.meetnow.Entity.User;

public interface UseremailRepository extends JpaRepository<User, Long> {
    User findByemail(String email); // 사용자 이메일로 조회
}