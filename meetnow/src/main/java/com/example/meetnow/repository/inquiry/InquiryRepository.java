package com.example.meetnow.repository.inquiry;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.meetnow.Entity.Inquiry;

public interface InquiryRepository extends JpaRepository<Inquiry, Integer> {
    List<Inquiry> findByUserIdOrderByCreatedAtDesc(String userId);
}
