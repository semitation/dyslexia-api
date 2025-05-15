package com.dyslexia.dyslexia.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
@Slf4j
@RequiredArgsConstructor
public class StorageService {

    @Value("${app.upload.dir:./uploads}")
    private String uploadDir;

    @PostConstruct
    public void init() {
        try {
            Path path = Paths.get(uploadDir);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
                log.info("업로드 디렉토리 생성 완료: {}", path.toAbsolutePath());
            }
            log.info("업로드 디렉토리 초기화가 완료되었습니다: {}", path.toAbsolutePath());
        } catch (IOException e) {
            log.error("업로드 디렉토리 초기화 실패", e);
            throw new RuntimeException("업로드 디렉토리를 생성할 수 없습니다.", e);
        }
    }

    public String store(MultipartFile file, String uniqueId, Long teacherId) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("파일이 비어 있습니다.");
        }

        try {
            log.info("파일 저장 요청 - 파일명: {}, 교사ID: {}, 업로드 디렉토리: {}", uniqueId, teacherId, uploadDir);
            
            String teacherFolderPath = teacherId.toString();
            Path teacherDir = Paths.get(uploadDir).resolve(teacherFolderPath);

            if (!Files.exists(teacherDir)) {
                Files.createDirectories(teacherDir);
                log.info("교사 디렉토리 생성 완료: {}", teacherDir.toAbsolutePath());
            }
            
            String pdfFolderName;
            if (uniqueId.contains(".")) {
                pdfFolderName = uniqueId.substring(0, uniqueId.lastIndexOf("."));
            } else {
                pdfFolderName = uniqueId;
            }
            
            Path pdfDir = teacherDir.resolve(pdfFolderName);

            if (!Files.exists(pdfDir)) {
                Files.createDirectories(pdfDir);
                log.info("PDF 디렉토리 생성 완료: {}", pdfDir.toAbsolutePath());
            }
            
            Path destinationFile = pdfDir.resolve(uniqueId)
                    .normalize().toAbsolutePath();

            if (!destinationFile.startsWith(pdfDir.toAbsolutePath())) {
                log.error("보안 오류: {} 파일을 {} 디렉토리 외부에 저장하려고 합니다", uniqueId, pdfDir);
                throw new IllegalStateException("보안 문제: 지정된 디렉토리 외부에 파일을 저장할 수 없습니다.");
            }
            
            Files.copy(file.getInputStream(), destinationFile, StandardCopyOption.REPLACE_EXISTING);
            
            log.info("파일 저장 완료: {}", destinationFile);
            
            return destinationFile.toString();
            
        } catch (IOException e) {
            log.error("파일 저장 중 오류 발생: {}", e.getMessage(), e);
            throw new IOException("파일을 저장할 수 없습니다: " + e.getMessage(), e);
        }
    }
    
    public Path load(String filePath) {
        Path path = Paths.get(filePath);
        log.debug("파일 로드 요청: {}", path.toAbsolutePath());
        return path;
    }

    public boolean exists(String filePath) {
        Path path = load(filePath);
        boolean exists = Files.exists(path);
        log.debug("파일 존재 확인: {}, 결과: {}", path.toAbsolutePath(), exists);
        return exists;
    }

    public boolean delete(String filePath) {
        try {
            Path file = load(filePath);
            log.info("파일 삭제 요청: {}", file.toAbsolutePath());
            boolean deleted = Files.deleteIfExists(file);
            log.info("파일 삭제 결과: {}", deleted ? "성공" : "실패");
            return deleted;
        } catch (IOException e) {
            log.error("파일 삭제 중 오류 발생: {}", filePath, e);
            return false;
        }
    }
}