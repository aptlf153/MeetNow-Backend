package com.example.meetnow.util;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.*;
import org.xml.sax.InputSource;
import java.io.StringReader;

@Component
public class ApiExplorer2 {

    private static final String serviceKey = "gzJCvTQ037DCxR3QkfMbVr9dPEN3Btz9gWmkCMer4V0OYCVg+XwInzs1fysw4V4lx5BR/K/Pe9bz4l0saTDkpQ==";

    public static String main(String date) throws IOException {
    	System.out.println(date + "0600");
    	StringBuilder urlBuilder = new StringBuilder("http://apis.data.go.kr/1360000/MidFcstInfoService/getMidLandFcst");
        urlBuilder.append("?" + URLEncoder.encode("serviceKey", "UTF-8") + "=" + URLEncoder.encode(serviceKey, "UTF-8"));
        urlBuilder.append("&" + URLEncoder.encode("pageNo", "UTF-8") + "=" + URLEncoder.encode("1", "UTF-8")); /* 페이지번호 */
        urlBuilder.append("&" + URLEncoder.encode("numOfRows", "UTF-8") + "=" + URLEncoder.encode("10", "UTF-8")); /* 한 페이지 결과 수 */
        urlBuilder.append("&" + URLEncoder.encode("dataType", "UTF-8") + "=" + URLEncoder.encode("XML", "UTF-8")); /* 요청자료형식(XML/JSON) Default: XML */
        urlBuilder.append("&" + URLEncoder.encode("stnId", "UTF-8") + "=" + URLEncoder.encode("108", "UTF-8")); /* 108 전국, 109 서울, 인천, 경기도 등 */
        urlBuilder.append("&" + URLEncoder.encode("tmFc", "UTF-8") + "=" + URLEncoder.encode(date + "0600", "UTF-8")); /* 발표시각을 입력 YYYYMMDD0600 (1800) */


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

        // XML 파싱 후 날씨 정보 리턴
        return parseWeatherCondition(sb.toString());
    }

    // XML을 파싱하여 날씨 상태 리턴
    private static String parseWeatherCondition(String xml) {
        try {
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                .parse(new InputSource(new StringReader(xml)));
            NodeList items = doc.getElementsByTagName("item");

            for (int i = 0; i < items.getLength(); i++) {
                Element item = (Element) items.item(i);
                String wfSv = item.getElementsByTagName("wfSv").item(0).getTextContent();
                return interpretWeather(wfSv); // 날씨 상태 반환
            }

            return "날씨 정보 없음"; // wfSv 정보가 없을 경우

        } catch (Exception e) {
            e.printStackTrace();
            return "날씨 정보 파싱 오류"; // XML 파싱 에러 처리
        }
    }

    // wfSv 값에 따른 날씨 상태 리턴
    private static String interpretWeather(String wfSv) {
        if (wfSv.contains("맑")) {
            return "맑음 🌞";
        } else if (wfSv.contains("구름")) {
            return "구름 많음 ☁️";
        } else if (wfSv.contains("비")) {
            return "비 ☔";
        } else if (wfSv.contains("눈")) {
            return "눈 ❄️";
        } else if (wfSv.contains("소나기")) {
            return "소나기 🌦️";
        } else {
            return "날씨 정보 불명 😕";
        }
    }
}
