package com.dyslexia.dyslexia.dto;

import com.dyslexia.dyslexia.entity.Page;
import com.dyslexia.dyslexia.entity.PageImage;
import com.dyslexia.dyslexia.entity.PageTip;
import com.dyslexia.dyslexia.entity.StudentPageProgress;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "페이지 상세 정보 응답 DTO")
public class PageDetailResponseDto {

  @Schema(description = "페이지 정보")
  private Page page;

  @Schema(description = "페이지 팁 목록")
  private List<PageTip> tips;

  @Schema(description = "페이지 이미지 목록")
  private List<PageImage> images;

  @Schema(description = "학생의 페이지 진행 상태")
  private StudentPageProgress progress;
}