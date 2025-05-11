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
        } catch (IOException e) {
            log.error("업로드 디렉토리 초기화 실패", e);
            throw new RuntimeException("업로드 디렉토리를 생성할 수 없습니다.", e);
        }
    }

    public String store(MultipartFile file, String filename) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("파일이 비어 있습니다.");
        }

        try {
            Path destinationFile = Paths.get(uploadDir).resolve(
                    Paths.get(filename))
                    .normalize().toAbsolutePath();

            Path uploadDirPath = Paths.get(uploadDir).toAbsolutePath();
            if (!destinationFile.getParent().equals(uploadDirPath)) {
                throw new IllegalStateException("보안 문제: 업로드 디렉토리 외부에 파일을 저장할 수 없습니다.");
            }
            Files.copy(file.getInputStream(), destinationFile, StandardCopyOption.REPLACE_EXISTING);
            
            log.info("파일 저장 완료: {}", destinationFile);
            
            return destinationFile.toString();
            
        } catch (IOException e) {
            log.error("파일 저장 중 오류 발생", e);
            throw new IOException("파일을 저장할 수 없습니다.", e);
        }
    }

    public Path load(String filename) {
        return Paths.get(uploadDir).resolve(filename);
    }

    public boolean exists(String filename) {
        return Files.exists(load(filename));
    }

    public boolean delete(String filename) {
        try {
            Path file = load(filename);
            return Files.deleteIfExists(file);
        } catch (IOException e) {
            log.error("파일 삭제 중 오류 발생: {}", filename, e);
            return false;
        }
    }
} 