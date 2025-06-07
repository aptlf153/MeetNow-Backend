package com.example.meetnow.service;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.PutObjectRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

@Service
public class S3Service {

    // AWS 자격 증명 및 버킷 이름, 리전 설정
    @Value("${aws.access-key-id}")
    private String accessKey;

    @Value("${aws.secret-access-key}")
    private String secretKey;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Value("${aws.region}")
    private String region;

    // 파일 업로드 메서드
    public String uploadFile(MultipartFile file) throws IOException {
        // AWS 자격 증명 설정
        BasicAWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, secretKey);

        // Amazon S3 클라이언트 생성 (Region 설정을 환경 변수로부터 가져옴)
        AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                .withRegion(region)  // 환경에서 가져온 region 사용
                .build();

        // 파일 업로드할 파일명 설정
        String fileName = "image/" + System.currentTimeMillis() + "_" + file.getOriginalFilename();  // 경로에 'image/' 추가

        // 파일 스트림 준비
        InputStream inputStream = file.getInputStream();

        try {
            // S3에 파일 업로드
            s3Client.putObject(new PutObjectRequest(bucketName, fileName, inputStream, null));
            
            // 업로드된 파일의 URL 반환
            return s3Client.getUrl(bucketName, fileName).toString();  // S3 URL
        } catch (Exception e) {
            // 예외 처리 (Amazon S3 서비스와 관련된 예외 처리 추가)
            throw new IOException("S3 파일 업로드 중 오류 발생", e);
        }
    }
}
