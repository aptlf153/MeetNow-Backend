package com.example.meetnow.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class MeetingService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // 모임 상태 변경 및 만료된 모임 삭제
    public void checkAndCloseExpiredMeetings() {
        // 오늘 날짜로부터 하루 지난 날짜 계산
        LocalDate today = LocalDate.now().plusDays(1);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String expirationDate = today.format(formatter);

        // 만료된 모임 찾기 (설정된 날짜가 오늘보다 하루 지났을 경우)
        String sql = "SELECT id FROM meeting WHERE meet_date = ? AND status = 'active'";
        List<Integer> expiredMeetings = jdbcTemplate.queryForList(sql, Integer.class, expirationDate);

        // 만료된 모임 삭제
        for (Integer meetingId : expiredMeetings) {
            String deleteSql = "DELETE FROM meeting WHERE id = ?";
            jdbcTemplate.update(deleteSql, meetingId);
        }

        System.out.println("Expired meetings deleted: " + expiredMeetings.size());
    }

    // 매일 자정에 실행될 스케줄러
    @Scheduled(cron = "00 00 00 * * ?") // 매일 자정에 실행
    public void scheduledTask() {
        checkAndCloseExpiredMeetings();  // 만료된 모임 확인 및 삭제
    }
}
