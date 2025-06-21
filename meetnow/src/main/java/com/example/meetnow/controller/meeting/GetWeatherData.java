package com.example.meetnow.controller.meeting;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.meetnow.util.api.ApiExplorer;
import com.example.meetnow.util.api.ApiExplorer2;
import com.example.meetnow.util.api.ApiExplorerShort;

import org.springframework.web.bind.annotation.CrossOrigin;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import org.springframework.beans.factory.annotation.Autowired;

@RestController
@CrossOrigin(origins = "http://localhost:5173") // React 앱의 URL
public class GetWeatherData {

    @Autowired
    private ApiExplorer apiexplorer;

    @Autowired
    private ApiExplorer2 apiexplorer2;

    @Autowired
    private ApiExplorerShort apiexplorerShort; // 단기예보용 클래스 (추가 필요)

    @PostMapping("/api/user/weather")
    public String getWeather(@RequestBody WeatherData data) throws IOException {
        String date = data.getDate();
        double latitude = Math.round(Double.parseDouble(data.getLatitude()) * 100.0) / 100.0;
        double longitude = Math.round(Double.parseDouble(data.getLongitude()) * 100.0) / 100.0;
        int nx = (int) Math.round(latitude);  // 실 사용 시 위경도 → x,y 변환 필요
        int ny = (int) Math.round(longitude);

        // 날짜 처리
        LocalDate today = LocalDate.now();
        LocalDate inputDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyyMMdd"));
        long daysDiff = ChronoUnit.DAYS.between(today, inputDate);

        // API 분기
        if (daysDiff == 0) {
            return "날씨 : " + apiexplorer.main(nx, ny, date); // 초단기 실황
        } else if (daysDiff == 1 || daysDiff == 2) {
            return "날씨 : " + apiexplorerShort.main(nx, ny, date); // 단기예보
        } else if (daysDiff >= 3 && daysDiff <= 10) {
            return "날씨 : " + apiexplorer2.main(nx, ny, date); // 중기예보
        } else {
            return "선택한 날짜는 예보 범위를 벗어났습니다.";
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
