package com.example.meetnow.util;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.io.BufferedReader;
import java.io.IOException;
import javax.xml.parsers.DocumentBuilderFactory;
import org.springframework.stereotype.Component;
import org.w3c.dom.*;
import org.xml.sax.InputSource;
import java.io.StringReader;

@Component
public class ApiExplorer2 {

    private static final String serviceKey = "gzJCvTQ037DCxR3QkfMbVr9dPEN3Btz9gWmkCMer4V0OYCVg+XwInzs1fysw4V4lx5BR/K/Pe9bz4l0saTDkpQ==";

    public String main(double lat, double lon, String selectedDate) throws IOException {
        String regId = getRegId(lat, lon);
        String tmFc = makeTmFc(); // 현재 기준 발표 시각
        String tmFcDate = tmFc.substring(0, 8); // 발표 날짜만 추출

        // URL 구성
        StringBuilder urlBuilder = new StringBuilder("https://apis.data.go.kr/1360000/MidFcstInfoService/getMidLandFcst");
        urlBuilder.append("?" + URLEncoder.encode("serviceKey", "UTF-8") + "=" + URLEncoder.encode(serviceKey, "UTF-8"));
        urlBuilder.append("&" + URLEncoder.encode("pageNo", "UTF-8") + "=1");
        urlBuilder.append("&" + URLEncoder.encode("numOfRows", "UTF-8") + "=10");
        urlBuilder.append("&" + URLEncoder.encode("dataType", "UTF-8") + "=XML");
        urlBuilder.append("&" + URLEncoder.encode("regId", "UTF-8") + "=" + URLEncoder.encode(regId, "UTF-8"));
        urlBuilder.append("&" + URLEncoder.encode("tmFc", "UTF-8") + "=" + URLEncoder.encode(tmFc, "UTF-8"));

        URL url = new URL(urlBuilder.toString());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Content-type", "application/json");

        BufferedReader rd;
        if (conn.getResponseCode() >= 200 && conn.getResponseCode() <= 300) {
            rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        } else {
            rd = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
        }

        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = rd.readLine()) != null) {
            sb.append(line);
        }
        rd.close();
        conn.disconnect();

        String xml = sb.toString();

        // NO_DATA 예외 처리
        if (xml.contains("<resultCode>03</resultCode>")) {
            return "선택한 날짜에는 중기예보 데이터가 없습니다.";
        }

        return getForecastForDate(xml, selectedDate, tmFcDate);
    }

    // 현재 날짜 기준 발표 시각 계산 (0600 또는 1800)
    private String makeTmFc() {
        LocalDateTime now = LocalDateTime.now();
        String baseTime;

        if (now.getHour() < 6) {
            baseTime = "1800";
            now = now.minusDays(1);
        } else if (now.getHour() < 18) {
            baseTime = "0600";
        } else {
            baseTime = "1800";
        }

        return now.format(DateTimeFormatter.ofPattern("yyyyMMdd")) + baseTime;
    }

    // 위도, 경도를 지역 코드로 변환
    private String getRegId(double lat, double lon) {
        if (lat >= 33 && lat <= 34) return "11G00000"; // 제주
        else if (lat >= 36 && lat <= 37.5 && lon >= 127 && lon <= 128) return "11C10000"; // 충북
        else if (lat >= 37.4 && lat <= 38 && lon >= 126 && lon <= 127.5) return "11B00000"; // 수도권
        else return "11C20000"; // 충남 (기본값)
    }

    // 날짜 차이 기반으로 wf3Am ~ wf10 중 하나를 추출
    public String getForecastForDate(String xml, String selectedDateStr, String tmFcDateStr) {
        try {
            LocalDate selectedDate = LocalDate.parse(selectedDateStr, DateTimeFormatter.ofPattern("yyyyMMdd"));
            LocalDate tmFcDate = LocalDate.parse(tmFcDateStr, DateTimeFormatter.ofPattern("yyyyMMdd"));

            long daysDiff = java.time.temporal.ChronoUnit.DAYS.between(tmFcDate, selectedDate);

            if (daysDiff < 3 || daysDiff > 10) {
                return "중기예보는 선택한 날짜에 대한 정보가 없습니다.";
            }

            String tagName = (daysDiff <= 7) ? "wf" + daysDiff + "Am" : "wf10";

            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                    .parse(new InputSource(new StringReader(xml)));
            NodeList items = doc.getElementsByTagName("item");

            for (int i = 0; i < items.getLength(); i++) {
                Element item = (Element) items.item(i);
                NodeList tagList = item.getElementsByTagName(tagName);
                if (tagList.getLength() > 0) {
                    String forecast = tagList.item(0).getTextContent();
                    return interpretWeather(forecast);
                }
            }

            return "해당 날짜에 대한 날씨 정보 없음";

        } catch (Exception e) {
            e.printStackTrace();
            return "날씨 정보 파싱 오류";
        }
    }

    // 날씨 텍스트를 이모지로 해석
    private String interpretWeather(String wf) {
        if (wf.contains("맑")) return "맑음 🌞";
        if (wf.contains("구름")) return "구름 많음 ☁️";
        if (wf.contains("비")) return "비 ☔";
        if (wf.contains("눈")) return "눈 ❄️";
        if (wf.contains("소나기")) return "소나기 🌦️";
        return "날씨 정보 불명 😕";
    }
}
