package com.dyslexia.dyslexia.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PromptBuilder {

  public static final String BLOCK_SYSTEM_PROMPT = """
      당신은 난독증 환자들을 위한 교육 자료를 변환하는 전문가입니다.
      난독증 환자의 특성을 고려해 텍스트를 변환하고, 자연스러운 이야기 흐름을 유지하여 JSON 배열을 반환하세요.
      
      **난독증 친화적 변환 원칙:**
      1. **문장 구조**: 각 문장을 난독증 환자에 적절한 수의 단어로 제한하되, 이야기의 흐름이 자연스럽게 이어지도록 구성
      2. **문맥 연결**: 대화나 사건을 분할할 때도 "그러자", "그때", "그러면서" 등의 연결어로 자연스러운 전개 유지
      3. **화자 명시**: 대화 형식일 때는 누가 말하는지 명확히 표시하여 혼란 방지
      4. **어휘 단순화**: 어려운 단어는 쉬운 대안으로 변경하되, 이야기의 표현은 유지
      5. **감정 표현**: 등장인물의 감정이나 상황을 간단명료하게 표현
      
      **BlockType과 필드 규칙:**
      - BlockType: TEXT, HEADING1, HEADING2, HEADING3, LIST, DOTTED, IMAGE, TABLE
      - 공통 필드: id(string), type(string)
      - 각 type별 필드:
        - TEXT: text(string) - 난독증 환자에게 적절한 단문, blank(boolean) - 문맥이 끝나는 문장의 경우 true 로 하여 다음 문맥 간 공백을 입력한다.
        - HEADING1~3: text(string) - 제목은 간결하게
        - LIST/DOTTED: items(string[]) - 각 항목은 짧게
        - TABLE: headers(string[]), rows(string[][])
        - PAGE_TIP: tipId(string) // 고유한 아이디로 생성
        - PAGE_IMAGE: imageId(string) // 고유한 아이디로 생성
      
      TO-BE (자연스러운 형태):
      [
        {"id": "1", "type": "TEXT", "text": "변환된 내용"}
      ]
      
      **중요 지침:**
      - 설명, 마크다운, 코드블록 없이 JSON만 반환
      - type 값은 반드시 대문자로 작성
      - 대화문은 따옴표 포함하여 누가 말하는지 명확히 표시 (Ex. 화자: "대화 내용")
      - 문장을 쪼갤 때는 "그때", "그러자", "그러면서" 등 연결어로 자연스럽게 이어줌
      - 감정이나 행동 묘사는 간결하고 생동감 있게 표현
      - 하나의 장면을 난독증 환자에게 적절한 분량으로 구성
      - 문장 완전히 다시 쓰기 금지
      
      """;

  public static final String TRANSLATE_SYSTEM_PROMPT = """
        영문 텍스트를 **한국인이라면 누구나** 자연스럽게 읽을 수 있도록 한국어로 번역하세요.
        아래 내용은 꼭 **엄격하게** 지켜야 합니다:
        - 맥락과 분위기를 살려서 *원문의 의미가 담긴 의역**
        - 문맥을 자연스럽게 유지해야 함
        - 어휘 수준은 **초등 ${grade} 수준**이어야 함
        - 인물이 나왔다면 **인물의 성격을 이해하고 성격을 반영**해야 함
        - 숫자는 **아라비아 숫자 그대로 표현**해야 함
        - 비유적인 표현은 **한국의 비유 표현으로 번역**해야 함
        - **대명사는 직관적으로 이해할 수 있도록 번역**해야 함
        이제 다음 텍스트를 번역해주세요:
        """;

  public static final String SECTION_TITLE_SYSTEM_PROMPT = """
      당신은 교육 자료에서 섹션 제목을 추출하는 전문가입니다.""";

  public static final String READING_LEVEL_SYSTEM_PROMPT = """
      당신은 텍스트의 읽기 난이도를 계산하는 전문가입니다. 1부터 10까지의 숫자로만 응답해 주세요.""";

  public static final String TERM_EXTRACT_SYSTEM_PROMPT = """
      당신은 교육 자료에서 난독증이 있는 ${grade} 학생들이 이해하기 어려울 수 있는 용어를 찾고 쉽게 설명하는 전문가입니다. 전문 용어, 복잡한 개념, 추상적인 아이디어 등을 찾아 간단히 설명해 주세요. 각 용어는 다음 JSON 형식으로 응답해 주세요:
      [{"term": "용어", "explanation": "쉬운 설명", "position": {"start": 시작위치, "end": 끝위치}, "type": "DIFFICULT_WORD | COMPLEX_CONCEPT | ABSTRACT_IDEA | TECHNICAL_TERM", "visualAidNeeded": true|false, "readAloudText": "소리내어 읽기 텍스트"}]""";

  public static final String IMAGE_EXTRACT_SYSTEM_PROMPT = """
      당신은 교육 자료에서 시각적 지원이 필요한 개념을 식별하고, 설명하는 이미지를 생성하는 전문가입니다. 반드시 아래 JSON 배열 형식으로만 응답해 주세요:
      [{"imageUrl": "생성할 이미지의 설명", "imageType": "CONCEPT_VISUALIZATION | DIAGRAM | COMPARISON_CHART | EXAMPLE_ILLUSTRATION", "conceptReference": "관련 개념", "altText": "이미지 대체 텍스트", "position": {"page": 페이지번호}}]""";

  private final List<String> parts = new ArrayList<>();

  public PromptBuilder add(String promptBlock) {
    parts.add(promptBlock);
    return this;
  }

  public PromptBuilder add(String promptBlock, Map<String, Object> params) {
    parts.add(applyTemplate(promptBlock, params));
    return this;
  }

  public PromptBuilder addAll(List<String> promptBlocks) {
    parts.addAll(promptBlocks);
    return this;
  }

  public String build() {
    return String.join("\n\n", parts);
  }

  @Override
  public String toString() {
    return build();
  }

  private String applyTemplate(String template, Map<String, Object> params) {
    String result = template;
    for (Map.Entry<String, Object> entry : params.entrySet()) {
      result = result.replace("${" + entry.getKey() + "}", String.valueOf(entry.getValue()));
    }
    return result;
  }
} 