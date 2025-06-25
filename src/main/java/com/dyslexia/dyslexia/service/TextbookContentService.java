package com.dyslexia.dyslexia.service;

import com.dyslexia.dyslexia.entity.Page;
import com.dyslexia.dyslexia.entity.PageImage;
import com.dyslexia.dyslexia.entity.PageTip;
import com.dyslexia.dyslexia.repository.PageImageRepository;
import com.dyslexia.dyslexia.repository.PageRepository;
import com.dyslexia.dyslexia.repository.PageTipRepository;
import com.dyslexia.dyslexia.repository.TextbookRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TextbookContentService {

  private final TextbookRepository textbookRepository;
  private final PageRepository pageRepository;
  private final PageTipRepository pageTipRepository;
  private final PageImageRepository pageImageRepository;

  public List<Page> getPagesByTextbookId(Long textbookId, Integer pageNumber) {
    if (pageNumber == null) {
      return pageRepository.findByTextbookIdOrderByPageNumberAsc(textbookId);
    }

    return textbookRepository.findById(textbookId)
        .map(textbook -> pageRepository.findByTextbookAndPageNumber(textbook, pageNumber)
            .map(List::of)
            .orElse(List.of()))
        .orElse(List.of());
  }

  public List<PageTip> getPageTipsByPageId(Long pageId) {
    return pageTipRepository.findByPageId(pageId);
  }

  public List<PageImage> getPageImagesByPageId(Long pageId) {
    return pageImageRepository.findByPageId(pageId);
  }
}