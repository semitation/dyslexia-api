package com.dyslexia.dyslexia.service;

import com.dyslexia.dyslexia.dto.PageDetailResponseDto;
import com.dyslexia.dyslexia.dto.PageDto;
import com.dyslexia.dyslexia.dto.PageProgressUpdateRequestDto;
import com.dyslexia.dyslexia.dto.TextbookDto;
import com.dyslexia.dyslexia.entity.Page;
import com.dyslexia.dyslexia.entity.PageImage;
import com.dyslexia.dyslexia.entity.PageTip;
import com.dyslexia.dyslexia.entity.Student;
import com.dyslexia.dyslexia.entity.StudentPageProgress;
import com.dyslexia.dyslexia.entity.StudentTextbookAssignment;
import com.dyslexia.dyslexia.mapper.PageMapper;
import com.dyslexia.dyslexia.mapper.TextbookMapper;
import com.dyslexia.dyslexia.repository.PageImageRepository;
import com.dyslexia.dyslexia.repository.PageRepository;
import com.dyslexia.dyslexia.repository.PageTipRepository;
import com.dyslexia.dyslexia.repository.StudentPageProgressRepository;
import com.dyslexia.dyslexia.repository.StudentRepository;
import com.dyslexia.dyslexia.repository.StudentTextbookAssignmentRepository;
import com.dyslexia.dyslexia.repository.TextbookRepository;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class StudentTextbookService {

  private final StudentRepository studentRepository;
  private final TextbookRepository textbookRepository;
  private final PageRepository pageRepository;
  private final PageTipRepository pageTipRepository;
  private final PageImageRepository pageImageRepository;
  private final StudentPageProgressRepository studentPageProgressRepository;
  private final StudentTextbookAssignmentRepository studentTextbookAssignmentRepository;

  private TextbookMapper textbookMapper;
  private PageMapper pageMapper;

  @Transactional(readOnly = true)
  public List<TextbookDto> getAssignedTextbooks(Long studentId) {
    return studentTextbookAssignmentRepository.findByStudentId(studentId).stream()
        .map(StudentTextbookAssignment::getTextbook)
        .map(textbookMapper::toDto)
        .toList();
  }

  @Transactional(readOnly = true)
  public List<PageDto> getTextbookPages(Long studentId, Long textbookId) {
    // 교재에 대한 학생의 접근 권한 확인
    studentTextbookAssignmentRepository.findByStudentIdAndTextbookId(studentId, textbookId)
        .orElseThrow(() -> new AccessDeniedException("이 문서에 대한 접근 권한이 없습니다."));

    // 교재 존재 여부 확인
    textbookRepository.findById(textbookId)
        .orElseThrow(() -> new IllegalArgumentException("해당 교재를 찾을 수 없습니다."));

    return pageRepository.findByTextbookIdOrderByPageNumberAsc(textbookId).stream()
        .map(pageMapper::toDto)
        .toList();
  }

  @Transactional(readOnly = true)
  public PageDetailResponseDto getPageDetail(Long studentId, Long pageId) {
    log.info("학생({}), 페이지({}) 페이지 상세 정보 조회", studentId, pageId);

    Page page = pageRepository.findById(pageId)
        .orElseThrow(() -> new IllegalArgumentException("페이지를 찾을 수 없습니다."));

    studentTextbookAssignmentRepository.findByStudentIdAndTextbookId(studentId, page.getTextbook().getId())
        .orElseThrow(() -> new AccessDeniedException("이 페이지에 대한 접근 권한이 없습니다."));

    List<PageTip> tips = pageTipRepository.findByPageId(pageId);
    List<PageImage> images = pageImageRepository.findByPageId(pageId);
    Optional<StudentPageProgress> progress = studentPageProgressRepository
        .findByStudentIdAndPageId(studentId, pageId);

    // 응답 구성
    return PageDetailResponseDto.builder()
        .page(page)
        .tips(tips)
        .images(images)
        .progress(progress.orElse(null))
        .build();
  }

  @Transactional
  public void updatePageProgress(Long studentId, Long pageId,
      PageProgressUpdateRequestDto request) {
    log.info("페이지 진행 상태 업데이트: 학생 ID: {}, 페이지 ID: {}", studentId, pageId);

    Page page = pageRepository.findById(pageId)
        .orElseThrow(() -> new IllegalArgumentException("페이지를 찾을 수 없습니다."));

    Student student = studentRepository.findById(studentId)
        .orElseThrow(() -> new IllegalArgumentException("학생을 찾을 수 없습니다."));

    studentTextbookAssignmentRepository.findByStudentIdAndTextbookId(studentId, page.getTextbook().getId())
        .orElseThrow(() -> new AccessDeniedException("이 페이지에 대한 접근 권한이 없습니다."));

    StudentPageProgress progress = studentPageProgressRepository
        .findByStudentIdAndPageId(studentId, pageId)
        .orElse(
            StudentPageProgress.builder()
                .student(student)
                .page(page)
                .build()
        );

    progress.setTimeSpentSeconds(request.getTimeSpentSeconds());
    progress.setCompletionStatus(request.getCompletionStatus());
    progress.setRetryCount(request.getRetryCount());
    progress.setComprehensionScore(request.getComprehensionScore());
    progress.setNotes(request.getNotes());
    progress.setLastAccessedAt(LocalDateTime.now());

    if (request.getUsedTipIds() != null && request.getUsedTipIds().length > 0) {
      String usedTipIdsStr = Arrays.stream(request.getUsedTipIds())
          .map(String::valueOf)
          .collect(Collectors.joining(","));
      progress.setUsedTipIds(usedTipIdsStr);
    }

    studentPageProgressRepository.save(progress);
  }

  /*@Transactional
  public ResponseEntity<ResponseDto> updateAccessibilitySettings(Long studentId, Long pageId,
      AccessibilitySettingsUpdateRequestDto request) {
    log.info("페이지 접근성 설정 업데이트: 학생 ID: {}, 페이지 ID: {}", studentId, pageId);

    Page page = pageRepository.findById(pageId)
        .orElseThrow(() -> new IllegalArgumentException("페이지를 찾을 수 없습니다."));

    Student student = studentRepository.findById(studentId)
        .orElseThrow(() -> new IllegalArgumentException("학생을 찾을 수 없습니다."));

    Textbook textbook = page.getTextbook();
    boolean hasAccess = studentTextbookAssignmentRepository
        .findByStudentIdAndTextbookId(studentId, textbook.getId())
        .isPresent();

    if (!hasAccess) {
      return ResponseEntity.status(403).body(
          ResponseDto.builder()
              .success(false)
              .message("이 페이지에 대한 접근 권한이 없습니다.")
              .build()
      );
    }

    if (Boolean.TRUE.equals(request.getApplyToEntireDocument())) {
      List<Page> documentPages = pageRepository.findByTextbookId(textbook.getId());

      for (Page docPage : documentPages) {
        updateSinglePageSettings(student, docPage, request);
      }

      return ResponseEntity.ok(
          ResponseDto.builder()
              .success(true)
              .message("문서 전체의 접근성 설정이 업데이트되었습니다.")
              .build()
      );
    } else {
      updateSinglePageSettings(student, page, request);

      return ResponseEntity.ok(
          ResponseDto.builder()
              .success(true)
              .message("페이지 접근성 설정이 업데이트되었습니다.")
              .build()
      );
    }
  }*/

  /*private void updateSinglePageSettings(Student student, Page page,
      AccessibilitySettingsUpdateRequestDto request) {
    PageAccessibilitySettings settings = pageAccessibilitySettingsRepository
        .findByStudentIdAndPageId(student.getId(), page.getId())
        .orElse(
            PageAccessibilitySettings.builder()
                .student(student)
                .page(page)
                .document(page.getTextbook().getDocument())
                .build()
        );

    settings.setFontSize(request.getFontSize());
    settings.setLineSpacing(request.getLineSpacing());
    settings.setLetterSpacing(request.getLetterSpacing());
    settings.setColorScheme(request.getColorScheme());
    settings.setTextToSpeechEnabled(request.getTextToSpeechEnabled());
    settings.setReadingHighlightEnabled(request.getReadingHighlightEnabled());
    settings.setCustomBackgroundColor(request.getCustomBackgroundColor());
    settings.setCustomTextColor(request.getCustomTextColor());
    settings.setLastModifiedAt(LocalDateTime.now());

    pageAccessibilitySettingsRepository.save(settings);

    if (Boolean.TRUE.equals(request.getSaveAsDefault())) {
      student.setDefaultFontSize(request.getFontSize());
      student.setDefaultLineSpacing(request.getLineSpacing());
      student.setDefaultLetterSpacing(request.getLetterSpacing());
      student.setDefaultColorScheme(request.getColorScheme());
      student.setDefaultTextToSpeechEnabled(request.getTextToSpeechEnabled());
      student.setDefaultReadingHighlightEnabled(request.getReadingHighlightEnabled());
      student.setDefaultBackgroundColor(request.getCustomBackgroundColor());
      student.setDefaultTextColor(request.getCustomTextColor());

      studentRepository.save(student);
    }
  }*/
} 