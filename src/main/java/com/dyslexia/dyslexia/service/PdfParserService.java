package com.dyslexia.dyslexia.service;

import com.dyslexia.dyslexia.exception.ApplicationException;
import com.dyslexia.dyslexia.exception.ExceptionCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class PdfParserService {

    public List<String> parsePages(String s3Url) throws IOException {
        if (s3Url == null || !s3Url.startsWith("http")) {
            throw new ApplicationException(ExceptionCode.INVALID_ARGUMENT);
        }
        
        List<String> pages = new ArrayList<>();
        
        PDDocument document = null;
        try {
            URL url = new URL(s3Url);
            try (InputStream inputStream = url.openStream()) {
                document = PDDocument.load(inputStream);
            }
            
            PDFTextStripper stripper = new PDFTextStripper();
            int pageCount = document.getNumberOfPages();
            
            for (int i = 1; i <= pageCount; i++) {
                stripper.setStartPage(i);
                stripper.setEndPage(i);
                String pageText = stripper.getText(document);
                pages.add(pageText);
                log.debug("페이지 {} 파싱 완료, 글자 수: {}", i, pageText.length());
            }
            
        } catch (Exception e) {
            log.error("S3 PDF 파싱 중 오류 발생: {}", s3Url, e);
            throw new IOException("S3에서 PDF를 파싱할 수 없습니다: " + e.getMessage(), e);
        } finally {
            if (document != null) {
                try {
                    document.close();
                } catch (IOException e) {
                    log.warn("PDF 문서 닫기 중 오류 발생", e);
                }
            }
        }
        
        return pages;
    }
    
}