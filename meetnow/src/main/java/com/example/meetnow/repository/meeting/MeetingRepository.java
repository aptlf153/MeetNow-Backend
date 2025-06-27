package com.example.meetnow.repository.meeting;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.meetnow.Entity.Meeting;

//✅ Repository
public interface MeetingRepository extends org.springframework.data.jpa.repository.JpaRepository<Meeting, Long> {
	List<Meeting> findByUserid(String userid);
 List<Meeting> findByTitleContainingOrDescriptionContainingOrLocationContainingAndClosed(String title, String description, String location, boolean closed);
	List<Meeting> findByClosed(boolean closed);
	
	@Query("SELECT m FROM Meeting m WHERE (m.title LIKE %:keyword% OR m.description LIKE %:keyword% OR m.location LIKE %:keyword% OR m.userid LIKE %:keyword%) AND m.closed = false")
	List<Meeting> searchNotClosedMeetings(@Param("keyword") String keyword);
}