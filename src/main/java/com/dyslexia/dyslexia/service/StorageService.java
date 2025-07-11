package com.dyslexia.dyslexia.service;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.dyslexia.dyslexia.exception.ApplicationException;
import com.dyslexia.dyslexia.exception.ExceptionCode;
import java.net.URL;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@Slf4j
@RequiredArgsConstructor
public class StorageService {

    private final AmazonS3Client amazonS3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;
    
    @Value("${cloud.aws.region.static}")
    private String awsRegion;

    public String store(MultipartFile file, String uniqueFilename, Long guardianId, Long documentId) throws IOException {
        if (file.isEmpty()) {
            throw new ApplicationException(ExceptionCode.INVALID_ARGUMENT);
        }

        try {
            String s3Key = String.format("%d/%d/%s", guardianId, documentId, uniqueFilename);

            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            metadata.setContentType(file.getContentType());
            metadata.setContentDisposition("inline; filename=\"" + uniqueFilename + "\"");

            PutObjectRequest putObjectRequest = new PutObjectRequest(
                bucketName,
                s3Key,
                file.getInputStream(),
                metadata
            );

            amazonS3Client.putObject(putObjectRequest);

            String publicS3Url = String.format("https://%s.s3.%s.amazonaws.com/%s",
                bucketName, getAwsRegion(), s3Key);
            
            return publicS3Url;

        } catch (Exception e) {
            log.error("S3 파일 업로드 중 오류 발생: {}", e.getMessage(), e);
            throw new ApplicationException(ExceptionCode.FILE_UPLOAD_FAILED);
        }
    }

    public String load(String s3KeyOrUrl) {
        try {
            String s3Key = extractS3KeyFromUrl(s3KeyOrUrl);
            log.debug("S3 파일 로드 요청: {}", s3Key);

            Date expiration = new Date();
            long expTimeMillis = expiration.getTime() + (1000 * 60 * 60); // 1시간
            expiration.setTime(expTimeMillis);

            GeneratePresignedUrlRequest generatePresignedUrlRequest =
                new GeneratePresignedUrlRequest(bucketName, s3Key)
                    .withMethod(HttpMethod.GET)
                    .withExpiration(expiration);

            URL presignedUrl = amazonS3Client.generatePresignedUrl(generatePresignedUrlRequest);
            String urlString = presignedUrl.toString();

            return urlString;

        } catch (Exception e) {
            log.error("S3 파일 로드 중 오류 발생: {}", s3KeyOrUrl, e);
            return null;
        }
    }

    public boolean exists(String s3KeyOrUrl) {
        try {
            String s3Key = extractS3KeyFromUrl(s3KeyOrUrl);

            boolean exists = amazonS3Client.doesObjectExist(bucketName, s3Key);
            return exists;

        } catch (Exception e) {
            log.error("S3 파일 존재 확인 중 오류 발생: {}", s3KeyOrUrl, e);
            return false;
        }
    }

    public boolean delete(String s3KeyOrUrl) {
        try {
            String s3Key = extractS3KeyFromUrl(s3KeyOrUrl);
            log.info("S3 파일 삭제 요청: {}", s3Key);

            if (!amazonS3Client.doesObjectExist(bucketName, s3Key)) {
                log.warn("삭제하려는 S3 파일이 존재하지 않습니다: {}", s3Key);
                return false;
            }

            amazonS3Client.deleteObject(bucketName, s3Key);
            return true;

        } catch (Exception e) {
            log.error("S3 파일 삭제 중 오류 발생: {}", s3KeyOrUrl, e);
            return false;
        }
    }

    private String extractS3KeyFromUrl(String s3KeyOrUrl) {
        if (s3KeyOrUrl == null) {
            throw new ApplicationException(ExceptionCode.INVALID_ARGUMENT);
        }

        if (!s3KeyOrUrl.startsWith("http")) {
            return s3KeyOrUrl;
        }

        try {
            URL url = new URL(s3KeyOrUrl);
            String path = url.getPath();
            return path.startsWith("/") ? path.substring(1) : path;
        } catch (Exception e) {
            log.error("S3 URL에서 키 추출 실패: {}", s3KeyOrUrl, e);
            return s3KeyOrUrl;
        }
    }
    
    private String getAwsRegion() {
        return awsRegion != null ? awsRegion : "ap-northeast-2";
    }
}
