package com.dyslexia.dyslexia.util;

public class FileHelper {
  /**
   * 파일 이름에서 확장자를 추출합니다.
   * 확장자가 없거나 잘못된 경우 빈 문자열("")을 반환합니다.
   *
   * @param filename 원본 파일 이름
   * @return 파일 확장자 (예: ".pdf")
   */
  public static String extractExtension(String filename) {
    if (filename == null || filename.isBlank()) {
      return "";
    }

    int dotIndex = filename.lastIndexOf('.');
    if (dotIndex == -1 || dotIndex == filename.length() - 1) {
      return "";
    }

    return filename.substring(dotIndex); // 확장자를 포함한 "." 반환
  }

  /**
   * 파일 경로에서 디렉토리 경로만 추출합니다.
   * 경로 구분자는 '/' 또는 '\\' 중 마지막에 나타나는 기준으로 판단합니다.
   *
   * @param filePath 전체 파일 경로
   * @return 폴더 경로 (파일 이름 제외)
   */
  public static String extractFolderPath(String filePath) {
    if (filePath == null || filePath.isBlank()) {
      return "";
    }

    int lastSlash = filePath.lastIndexOf('/');
    int lastBackslash = filePath.lastIndexOf('\\');
    int splitIndex = Math.max(lastSlash, lastBackslash);

    if (splitIndex == -1) {
      return "";
    }

    return filePath.substring(0, splitIndex);
  }

  /**
   * 파일 경로에서 디렉토리 경로만 추출합니다.
   * 경로 구분자는 '/' 또는 '\\' 중 마지막에 나타나는 기준으로 판단합니다.
   *
   * @param filePath 전체 파일 경로
   * @return 파일 이름 (경로 제외)
   */
  public static String extractFileName(String filePath) {
    if (filePath == null || filePath.isBlank()) {
      return "";
    }

    int lastSlash = filePath.lastIndexOf('/');
    int lastBackslash = filePath.lastIndexOf('\\');
    int splitIndex = Math.max(lastSlash, lastBackslash);

    if (splitIndex == -1) {
      return "";
    }

    return filePath.substring(splitIndex + 1);
  }
}
