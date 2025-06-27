package com.example.meetnow.util.api;

import org.springframework.stereotype.Component;
import org.w3c.dom.*;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

@Component
public class ApiExplorerShort {

    private static final String serviceKey = "gzJCvTQ037DCxR3QkfMbVr9dPEN3Btz9gWmkCMer4V0OYCVg+XwInzs1fysw4V4lx5BR/K/Pe9bz4l0saTDkpQ==";

    public String main(int nx, int ny, String targetDate) throws IOException {
        // 오늘 날짜를 baseDate로 고정
        LocalDate base = LocalDate.now();
        String baseDate = base.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String baseTime = getLatestBaseTime(); // 현재 시간 기준 가장 최신 발표 시간 선택

        // URL 구성
        StringBuilder urlBuilder = new StringBuilder("https://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getVilageFcst");
        urlBuilder.append("?" + URLEncoder.encode("serviceKey", "UTF-8") + "=" + URLEncoder.encode(serviceKey, "UTF-8"));
        urlBuilder.append("&" + URLEncoder.encode("pageNo", "UTF-8") + "=1");
        urlBuilder.append("&" + URLEncoder.encode("numOfRows", "UTF-8") + "=200");
        urlBuilder.append("&" + URLEncoder.encode("dataType", "UTF-8") + "=XML");
        urlBuilder.append("&" + URLEncoder.encode("base_date", "UTF-8") + "=" + URLEncoder.encode(baseDate, "UTF-8"));
        urlBuilder.append("&" + URLEncoder.encode("base_time", "UTF-8") + "=" + URLEncoder.encode(baseTime, "UTF-8"));
        urlBuilder.append("&" + URLEncoder.encode("nx", "UTF-8") + "=" + nx);
        urlBuilder.append("&" + URLEncoder.encode("ny", "UTF-8") + "=" + ny);

        URL url = new URL(urlBuilder.toString());
        System.out.println("📌 API 요청 URL: " + urlBuilder.toString());

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Content-type", "application/json");

        BufferedReader rd = new BufferedReader(new InputStreamReader(
                conn.getResponseCode() >= 200 && conn.getResponseCode() <= 300
                        ? conn.getInputStream()
                        : conn.getErrorStream()
        ));

        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = rd.readLine()) != null) {
            sb.append(line);
        }
        rd.close();
        conn.disconnect();

        return parseWeather(sb.toString(), targetDate);
    }

    // 🔸 base_time을 현재 시각에 따라 동적으로 반환
    private String getLatestBaseTime() {
        LocalTime now = LocalTime.now();

        if (now.isAfter(LocalTime.of(23, 10))) return "2300";
        else if (now.isAfter(LocalTime.of(20, 10))) return "2000";
        else if (now.isAfter(LocalTime.of(17, 10))) return "1700";
        else if (now.isAfter(LocalTime.of(14, 10))) return "1400";
        else if (now.isAfter(LocalTime.of(11, 10))) return "1100";
        else if (now.isAfter(LocalTime.of(8, 10))) return "0800";
        else if (now.isAfter(LocalTime.of(5, 10))) return "0500";
        else if (now.isAfter(LocalTime.of(2, 10))) return "0200";
        else return "2300"; // 전날 밤 11시 발표
    }

    private String parseWeather(String xml, String targetDate) {
        try {
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                    .parse(new InputSource(new StringReader(xml)));
            NodeList items = doc.getElementsByTagName("item");

            String sky = null;
            String pty = null;

            LocalDate target = LocalDate.parse(targetDate, DateTimeFormatter.ofPattern("yyyyMMdd"));
            LocalDate today = LocalDate.now();
            long daysBetween = ChronoUnit.DAYS.between(today, target);

            if (daysBetween != 1 && daysBetween != 2) {
                return "⚠️ 예보 지원 날짜가 아닙니다 (내일 또는 모레만 가능)";
            }

            for (int i = 0; i < items.getLength(); i++) {
                Element item = (Element) items.item(i);
                String fcstDate = getTagValue(item, "fcstDate");
                String category = getTagValue(item, "category");
                String fcstValue = getTagValue(item, "fcstValue");

                if (!targetDate.equals(fcstDate)) continue;

                if ("SKY".equals(category) && sky == null) {
                    sky = interpretSKY(fcstValue);
                }
                if ("PTY".equals(category) && pty == null) {
                    pty = interpretPTY(fcstValue);
                }

                if (sky != null && pty != null) break;
            }

            if (pty != null && !"맑음 🌞".equals(pty)) {
                return pty;
            } else if (sky != null) {
                return sky;
            } else {
                return "⚠️ 해당 날짜의 예보 정보 없음";
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "❌ 날씨 정보 파싱 오류";
        }
    }

    private String getTagValue(Element element, String tagName) {
        NodeList tag = element.getElementsByTagName(tagName);
        return tag.getLength() > 0 ? tag.item(0).getTextContent() : null;
    }

    private String interpretPTY(String code) {
        switch (code) {
            case "0": return "맑음 🌞";
            case "1": return "비 ☔";
            case "2": return "비/눈 🌧️❄️";
            case "3": return "눈 ❄️";
            case "4": return "소나기 🌦️";
            default: return "불명확한 날씨";
        }
    }

    private String interpretSKY(String code) {
        switch (code) {
            case "1": return "맑음 🌞";
            case "3": return "구름 많음 ☁️";
            case "4": return "흐림 🌫️";
            default: return "하늘 상태 불명";
        }
    }
}
