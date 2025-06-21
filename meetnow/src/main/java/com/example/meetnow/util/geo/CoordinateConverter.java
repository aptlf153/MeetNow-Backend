package com.example.meetnow.util.geo;

public class CoordinateConverter {

    private static final double RE = 6371.00877; // 지구 반지름 (km)
    private static final double GRID = 5.0; // 격자 크기 (5km)
    private static final double SLAT1 = 30.0; // 표준 위도 1
    private static final double SLAT2 = 60.0; // 표준 위도 2
    private static final double OLON = 126.0; // 기준 경도
    private static final double OLAT = 38.0; // 기준 위도
    private static final double XO = 43; // 기준 좌표 X
    private static final double YO = 136; // 기준 좌표 Y

    // 위도, 경도를 격자 좌표로 변환하는 함수
    public static int[] convertLatLonToGrid(double lat, double lon) {
        double radLat = lat * Math.PI / 180.0;
        double radLon = lon * Math.PI / 180.0;
        double sn = Math.sin(radLat);
        double sf = Math.sin(Math.toRadians(SLAT1));
        double s = Math.sin(Math.toRadians(SLAT2));

        double latitude = (lat - OLAT) * (Math.PI / 180.0);
        double longitude = (lon - OLON) * (Math.PI / 180.0);

        // 계산된 nx, ny 값
        double nx = (longitude * Math.cos(radLat) * RE / GRID) + XO;
        double ny = (latitude * Math.cos(lat) * RE / GRID) + YO;

        return new int[]{(int) nx, (int) ny};
    }

}
