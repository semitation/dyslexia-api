package com.dyslexia.dyslexia.controller;

import com.dyslexia.dyslexia.dto.DocumentDto;
import com.dyslexia.dyslexia.dto.DocumentResponseDto;
import com.dyslexia.dyslexia.entity.Document;
import com.dyslexia.dyslexia.enums.Grade;
import com.dyslexia.dyslexia.service.ConvertProcessService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Tag(name = "Document", description = "PDF 문서 관리 API")
@RestController
@RequestMapping("documents")
@RequiredArgsConstructor
@Slf4j
public class DocumentController {

    private final ConvertProcessService convertProcessService;

    @Operation(summary = "PDF 문서 업로드", description = "보호자가 PDF 문서를 업로드하고 처리를 시작합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "업로드 성공", 
                     content = @Content(schema = @Schema(implementation = DocumentResponseDto.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentResponseDto> uploadDocument(
            @Parameter(description = "보호자 ID", required = true)
            @RequestParam("guardianId") Long guardianId,
            
            @Parameter(description = "PDF 파일", required = true) 
            @RequestParam("file") MultipartFile file,
            
            @Parameter(description = "문서 제목", required = true) 
            @RequestParam("title") String title,
            
            @Parameter(description = "학년", required = true) 
            @RequestParam("grade") Grade grade
    ) {
        try {
            log.info("문서 업로드 요청: 보호자 ID: {}, 제목: {}, 학년: {}", guardianId, title, grade);
            
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".pdf")) {
                return ResponseEntity.badRequest().body(
                    DocumentResponseDto.builder()
                        .success(false)
                        .message("PDF 파일만 업로드 가능합니다.")
                        .build()
                );
            }
            
            Document document = convertProcessService.uploadDocument(guardianId, file, title, grade);
            
            DocumentResponseDto responseDto = DocumentResponseDto.builder()
                .success(true)
                .message("PDF 업로드 완료. 비동기 처리가 시작되었습니다.")
                .document(mapToDto(document))
                .build();
                
            return ResponseEntity.ok(responseDto);
            
        } catch (IOException e) {
            log.error("파일 업로드 중 오류 발생", e);
            return ResponseEntity.status(500).body(
                DocumentResponseDto.builder()
                    .success(false)
                    .message("파일 업로드 중 오류가 발생했습니다: " + e.getMessage())
                    .build()
            );
        } catch (Exception e) {
            log.error("문서 처리 중 오류 발생", e);
            return ResponseEntity.status(500).body(
                DocumentResponseDto.builder()
                    .success(false)
                    .message("문서 처리 중 오류가 발생했습니다: " + e.getMessage())
                    .build()
            );
        }
    }
    
    private DocumentDto mapToDto(Document document) {
        return DocumentDto.builder()
            .id(document.getId())
            .guardianId(document.getGuardian().getId())
            .title(document.getTitle())
            .originalFilename(document.getOriginalFilename())
            .fileSize(document.getFileSize())
            .uploadedAt(document.getUploadedAt())
            .build();
    }
} 