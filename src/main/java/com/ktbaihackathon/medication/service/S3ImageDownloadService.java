package com.ktbaihackathon.medication.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.util.Base64;

@Service
@RequiredArgsConstructor
public class S3ImageDownloadService {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket}")
    private String bucket;

    public String downloadAsBase64(String key) {
        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        // 🚨 catch 블록을 Exception e 로 교체해서 모든 에러(AWS 예외 포함)를 잡아냅니다.
        try (ResponseInputStream<GetObjectResponse> s3Object = s3Client.getObject(request)) {
            byte[] bytes = s3Object.readAllBytes();
            return Base64.getEncoder().encodeToString(bytes);
        } catch (Exception e) {
            System.out.println("============== [S3 에러 발생!!!] ==============");
            e.printStackTrace(); // 이제 AWS S3 에러 원인이 콘솔에 통째로 찍힙니다.
            System.out.println("=============================================");
            throw new RuntimeException("S3 이미지 다운로드 실패: " + key, e);
        }
    }
}