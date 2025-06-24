package com.dyslexia.dyslexia.dto;

import com.dyslexia.dyslexia.enums.ConvertProcessStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "교재 처리 상태 응답 DTO")
public class ConvertProcessStatusDto {

    @Schema(description = "교재 ID", example = "1")
    private Long textbookId;

    @Schema(description = "처리 상태", example = "PROCESSING")
    private ConvertProcessStatus status;

    @Schema(description = "처리 진행도 (0-100)", example = "65")
    private Integer progress;
} 