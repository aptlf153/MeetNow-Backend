package com.example.meetnow.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.meetnow.util.ApiExplorer;
import com.example.meetnow.util.ApiExplorer2;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;

@RestController
@CrossOrigin(origins = "http://localhost:5173") // React 앱의 URL
public class GetWeatherData {

	@Autowired
	private ApiExplorer apiexplorer;
	
	@Autowired
	private ApiExplorer2 apiexplorer2;
	
	@PostMapping("/api/user/weather")
    public String signup(@RequestBody WeatherData data) throws IOException {
		
        String date = data.getDate();
        double latitude = Math.round(Double.parseDouble(data.getLatitude()) * 100.0) / 100.0;
        double longitude = Math.round(Double.parseDouble(data.getLongitude()) * 100.0) / 100.0;
        int nx = (int) Math.round(latitude);
        int ny = (int) Math.round(longitude);
        
        // 오늘 날짜 구하기
        LocalDate today = LocalDate.now();

        // 입력된 날짜를 LocalDate로 변환하기 위한 포맷 설정
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

        // 입력된 날짜 문자열을 LocalDate로 변환
        LocalDate inputDate = LocalDate.parse(date, formatter);

        // 비교
        if (inputDate.isEqual(today)) {
        	return "날씨 : " + apiexplorer.main(nx,ny, date);
        } else if (inputDate.isAfter(today)) {
        	return "날씨 : " + apiexplorer2.main(date);
        } else {
        	return "알수없음";
        }    
	}

    // 요청 데이터를 담을 클래스
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
