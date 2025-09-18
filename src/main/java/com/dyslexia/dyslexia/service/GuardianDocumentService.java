package com.dyslexia.dyslexia.service;

import com.dyslexia.dyslexia.dto.DocumentDto;
import com.dyslexia.dyslexia.entity.Document;
import com.dyslexia.dyslexia.entity.Guardian;
import com.dyslexia.dyslexia.exception.ApplicationException;
import com.dyslexia.dyslexia.exception.ExceptionCode;
import com.dyslexia.dyslexia.mapper.DocumentMapper;
import com.dyslexia.dyslexia.repository.DocumentRepository;
import com.dyslexia.dyslexia.repository.GuardianRepository;
import com.dyslexia.dyslexia.util.JwtTokenProvider;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class GuardianDocumentService {

    private final GuardianRepository guardianRepository;
    private final DocumentRepository documentRepository;
    private final DocumentMapper documentMapper;
    private final JwtTokenProvider jwtTokenProvider;

    public List<DocumentDto> getMyDocuments() {
        String currentClientId = jwtTokenProvider.getCurrentClientId();
        Guardian guardian = guardianRepository.findByClientId(currentClientId)
            .orElseThrow(() -> new ApplicationException(ExceptionCode.GUARDIAN_NOT_FOUND));

        List<Document> documents = documentRepository.findByGuardianIdOrderByUploadedAtDesc(guardian.getId());
        return documents.stream()
            .map(documentMapper::toDto)
            .toList();
    }
}

