package com.example.meetnow.controller.meeting;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.meetnow.util.api.ApiExplorer;
import com.example.meetnow.util.api.ApiExplorer2;
import com.example.meetnow.util.api.ApiExplorerShort;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

@RestController
@RequestMapping("/api/user") // ✅ 이 부분 추가로 /api/user/weather 경로 활성화
public class GetWeatherData {

    @Autowired
    private ApiExplorer apiexplorer;

    @Autowired
    private ApiExplorer2 apiexplorer2;

    @Autowired
    private ApiExplorerShort apiexplorerShort;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @PostMapping("/weather")
    public String getWeather(@RequestBody WeatherData data) {
        long start = System.currentTimeMillis();

        try {
            String date = data.getDate();
            double latitude = Math.round(Double.parseDouble(data.getLatitude()) * 100.0) / 100.0;
            double longitude = Math.round(Double.parseDouble(data.getLongitude()) * 100.0) / 100.0;
            int nx = (int) Math.round(latitude);
            int ny = (int) Math.round(longitude);

            LocalDate today = LocalDate.now();
            LocalDate inputDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyyMMdd"));
            long daysDiff = ChronoUnit.DAYS.between(today, inputDate);

            String cacheKey = "weather:" + nx + ":" + ny + ":" + date;
            String cached = redisTemplate.opsForValue().get(cacheKey);

            if (cached != null) {
                long duration = System.currentTimeMillis() - start;
                System.out.println("[CACHE HIT] 응답 시간: " + duration + "ms");
                return "날씨 : " + cached;
            }

            String result;
            if (daysDiff == 0) {
                result = apiexplorer.main(nx, ny, date);
            } else if (daysDiff == 1 || daysDiff == 2) {
                result = apiexplorerShort.main(nx, ny, date);
            } else if (daysDiff >= 3 && daysDiff <= 10) {
                result = apiexplorer2.main(nx, ny, date);
            } else {
                return "선택한 날짜는 예보 범위를 벗어났습니다.";
            }

            if (result == null || result.trim().isEmpty()) {
                return "날씨 정보를 불러오지 못했습니다.";
            }

            redisTemplate.opsForValue().set(cacheKey, result, java.time.Duration.ofHours(1));

            long duration = System.currentTimeMillis() - start;
            System.out.println("[CACHE MISS] 응답 시간: " + duration + "ms");

            return "날씨 : " + result;

        } catch (Exception e) {
            e.printStackTrace();
            return "날씨 API 호출 중 오류 발생: " + e.getMessage();
        }
    }

    // 요청 DTO
    static class WeatherData {
        private String latitude;
        private String longitude;
        private String Date;

        public String getLatitude() {
            return latitude;
        }

        public void setLatitude(String latitude) {
            this.latitude = latitude;
        }

        public String getLongitude() {
            return longitude;
        }

        public void setLongitude(String longitude) {
            this.longitude = longitude;
        }

        public String getDate() {
            return Date;
        }

        public void setDate(String Date) {
            this.Date = Date;
        }
    }
}
