package com.dyslexia.dyslexia.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DocumentCompleteRequestDto {

    @NotBlank
    @Size(min = 1, max = 200)
    @JsonAlias({"jobId", "job_id"})
    private String jobId;

    @NotBlank
    @Size(min = 1, max = 500)
    @JsonAlias({"pdfName", "pdf_name"})
    private String pdfName;

    @NotNull
    private JsonNode data;
}

