package com.example.meetnow.service.meeting;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class MeetingSchedulerService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // 매일 자정에 마감 처리
    @Scheduled(cron = "0 0 0 * * *")
    public void autoCloseMeetings() {
        String sql = "UPDATE meet SET closed = 1 WHERE meet_date < NOW() AND closed = 0";
        int updated = jdbcTemplate.update(sql);
        System.out.println("[자동 마감] 처리된 모임 수: " + updated);
    }

    // 매일 새벽 1시에 삭제 처리
    @Scheduled(cron = "0 0 0 * * *")
    public void deleteExpiredMeetings() {
        String sql = "DELETE FROM meet WHERE closed = 1 AND meet_date < NOW() - INTERVAL 1 DAY";
        int deleted = jdbcTemplate.update(sql);
        System.out.println("[자동 삭제] 삭제된 마감 모임 수: " + deleted);
    }
}
