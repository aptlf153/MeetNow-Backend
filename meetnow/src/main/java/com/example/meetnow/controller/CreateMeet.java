package com.example.meetnow.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.example.meetnow.service.S3Service;
import com.example.meetnow.util.JwtUtil;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
public class CreateMeet {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private S3Service s3Service;

    @PostMapping("/api/user/createmeet")
    public String create(
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("latitude") String latitude,
            @RequestParam("longitude") String longitude,
            @RequestParam("meetlocation") String meetlocation,
            @RequestParam("date") String meetdate,
            HttpServletRequest request) throws IOException {

        // JWT 쿠키 추출
        Cookie[] cookies = request.getCookies();
        String refreshToken = null;

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("refreshToken".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                }
            }
        }

        String userid = jwtUtil.extractUsername(refreshToken);

        // ✅ 오늘 날짜에 생성된 모임 수 확인
        String countSql = "SELECT COUNT(*) FROM meet WHERE userid = ? AND DATE(meet_date) = CURDATE()";
        int todayCount = jdbcTemplate.queryForObject(countSql, Integer.class, userid);

        if (todayCount >= 5) {
            return "하루에 최대 5개의 모임만 생성할 수 있습니다.";
        }

        // ✅ 이미지 업로드 또는 기본 이미지 처리
        String fileUrl;
        if (file != null && !file.isEmpty()) {
            fileUrl = s3Service.uploadFile(file);
        } else {
            fileUrl = "https://your-bucket.s3.amazonaws.com/images/default-banner.jpg";
        }

        // ✅ DB 저장
        String sql = "INSERT INTO meet (userid, title, description, latitude, longitude, location, meet_date, image_url, closed) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, false)";

        jdbcTemplate.update(
                sql,
                userid,
                title,
                description,
                Double.parseDouble(latitude),
                Double.parseDouble(longitude),
                meetlocation,
                Date.valueOf(formatDate(meetdate)),
                fileUrl
        );

        return "모임 저장 완료!";
    }

    private String formatDate(String meetdate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        LocalDate localDate = LocalDate.parse(meetdate, formatter);
        return localDate.toString(); // yyyy-MM-dd
    }
}
