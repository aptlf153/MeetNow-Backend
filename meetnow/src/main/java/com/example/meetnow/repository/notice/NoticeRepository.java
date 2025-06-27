package com.example.meetnow.repository.notice;

import com.example.meetnow.Entity.Notice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoticeRepository extends JpaRepository<Notice, Integer> {
}
