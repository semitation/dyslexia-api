package com.dyslexia.dyslexia.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.net.URL;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3JsonProcessingService {

    private final AmazonS3 amazonS3;
    private final ObjectMapper objectMapper;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    public JsonNode downloadAndParseJson(String s3Url) {
        try {
            log.info("S3에서 JSON 파일 다운로드 시작: {}", s3Url);

            String s3Key = extractS3KeyFromUrl(s3Url);

            S3Object s3Object = amazonS3.getObject(bucketName, s3Key);
            InputStream inputStream = s3Object.getObjectContent();

            JsonNode jsonNode = objectMapper.readTree(inputStream);

            log.info("S3 JSON 파일 파싱 완료. Key: {}", s3Key);

            return jsonNode;

        } catch (Exception e) {
            log.error("S3 JSON 파일 다운로드/파싱 실패: {}", s3Url, e);
            throw new RuntimeException("S3 파일 처리 중 오류 발생", e);
        }
    }

    private String extractS3KeyFromUrl(String s3Url) {
        try {
            URL url = new URL(s3Url);
            String path = url.getPath();

            if (path.startsWith("/")) {
                path = path.substring(1);
            }

            return path;

        } catch (Exception e) {
            log.error("S3 URL에서 키 추출 실패: {}", s3Url, e);
            throw new IllegalArgumentException("잘못된 S3 URL 형식", e);
        }
    }
}