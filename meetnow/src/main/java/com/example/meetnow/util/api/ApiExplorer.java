package com.example.meetnow.util.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import javax.xml.parsers.DocumentBuilderFactory;

import org.springframework.stereotype.Component;
import org.w3c.dom.*;
import org.xml.sax.InputSource;

@Component
public class ApiExplorer {

    private static final String serviceKey = "gzJCvTQ037DCxR3QkfMbVr9dPEN3Btz9gWmkCMer4V0OYCVg+XwInzs1fysw4V4lx5BR/K/Pe9bz4l0saTDkpQ==";

    public static String main(int nx, int ny, String date) throws IOException {
        // URL 구성
        StringBuilder urlBuilder = new StringBuilder("http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getUltraSrtNcst");
        urlBuilder.append("?" + URLEncoder.encode("serviceKey", "UTF-8") + "=" + URLEncoder.encode(serviceKey, "UTF-8"));
        urlBuilder.append("&" + URLEncoder.encode("pageNo", "UTF-8") + "=1");
        urlBuilder.append("&" + URLEncoder.encode("numOfRows", "UTF-8") + "=1000");
        urlBuilder.append("&" + URLEncoder.encode("dataType", "UTF-8") + "=XML");
        urlBuilder.append("&base_date=" + URLEncoder.encode(date, "UTF-8"));
        urlBuilder.append("&" + URLEncoder.encode("base_time", "UTF-8") + "=0600");
        urlBuilder.append("&nx=" + URLEncoder.encode(String.valueOf(nx), "UTF-8"));
        urlBuilder.append("&ny=" + URLEncoder.encode(String.valueOf(ny), "UTF-8"));

        // HTTP 요청
        URL url = new URL(urlBuilder.toString());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Content-type", "application/json");

        BufferedReader rd = (conn.getResponseCode() >= 200 && conn.getResponseCode() <= 300)
            ? new BufferedReader(new InputStreamReader(conn.getInputStream()))
            : new BufferedReader(new InputStreamReader(conn.getErrorStream()));

        // 응답 읽기
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = rd.readLine()) != null) {
            sb.append(line);
        }
        rd.close();
        conn.disconnect();

        // XML 파싱하여 PTY 값 추출
        return parsePTY(sb.toString());
    }

    private static String parsePTY(String xml) {
        try {
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                .parse(new InputSource(new StringReader(xml)));
            NodeList items = doc.getElementsByTagName("item");

            for (int i = 0; i < items.getLength(); i++) {
                Element item = (Element) items.item(i);
                String category = item.getElementsByTagName("category").item(0).getTextContent();
                if ("PTY".equals(category)) {
                    String value = item.getElementsByTagName("obsrValue").item(0).getTextContent();
                    return interpretPTY(value);
                }
            }
            return "PTY 정보 없음";
        } catch (Exception e) {
            e.printStackTrace();
            return "날씨 정보 파싱 오류";
        }
    }

    private static String interpretPTY(String value) {
        switch (value) {
            case "0": return "맑음 🌞";
            case "1": return "비 ☔";
            case "2": return "비/눈 🌧️❄️";
            case "3": return "눈 ❄️";
            case "4": return "소나기 🌦️";
            default: return "날씨 정보 불명 😕";
        }
    }
}
