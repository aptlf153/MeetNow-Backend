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
@CrossOrigin(origins = "http://localhost:5173") // React 앱의 URL
public class CreateMeet {

    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Autowired
    private S3Service s3Service;    

    @PostMapping("/api/user/createmeet")
    public String create(
            @RequestParam("file") MultipartFile file,  // 파일 업로드 처리
            @RequestParam("title") String title,  // 모임 제목
            @RequestParam("description") String description,  // 모임 설명
            @RequestParam("latitude") String latitude,  // 위도
            @RequestParam("longitude") String longitude,  // 경도
            @RequestParam("meetlocation") String meetlocation,  // 모임 장소
            @RequestParam("date") String meetdate,  // 날짜
            HttpServletRequest request) throws IOException {

        // 쿠키 정보 처리 및 JWT 사용 등은 그대로 유지
        Cookie[] cookies = request.getCookies();
        String accessToken = null;
        String refreshToken = null;

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("accessToken".equals(cookie.getName())) {
                    accessToken = cookie.getValue(); // accessToken 값 저장
                } else if ("refreshToken".equals(cookie.getName())) {
                    refreshToken = cookie.getValue(); // refreshToken 값 저장
                }
            }
        }

        String userid = jwtUtil.extractUsername(refreshToken);

        // 파일 업로드 후 URL 얻기
        String fileUrl = s3Service.uploadFile(file);  // S3Service로 파일을 업로드하고 URL 반환

        // 모임 정보 DB에 저장
        String sql = "INSERT INTO meet (userid, title, description, latitude, longitude, location, meet_date, image_url) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        jdbcTemplate.update(
                sql,
                userid,
                title,
                description,
                Double.parseDouble(latitude),
                Double.parseDouble(longitude),
                meetlocation,
                Date.valueOf(formatDate(meetdate)), // 날짜 포맷 변환
                fileUrl // 업로드된 이미지 URL 저장
        );

        return "모임 저장 완료!";
    }

    private String formatDate(String meetdate) {
        // 'yyyyMMdd' 형식에 맞는 DateTimeFormatter 정의
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        // 문자열을 LocalDate로 변환
        LocalDate localDate = LocalDate.parse(meetdate, formatter);
        // LocalDate를 String으로 반환
        return localDate.toString();
    }
}
