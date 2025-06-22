package com.dyslexia.dyslexia.util;

public class ConvertProcessHolder {

  private static final ThreadLocal<Long> textbookId = new ThreadLocal<>();
  private static final ThreadLocal<Long> guardianId = new ThreadLocal<>();

  private static final ThreadLocal<Integer> pageNumber = new ThreadLocal<>();

  public static Long getTextbookId() {
    return textbookId.get();
  }

  public static void setTextbookId(Long id) {
    textbookId.set(id);
  }

  public static Long getGuardianId() {
    return guardianId.get();
  }

  public static void setGuardianId(Long id) {
    guardianId.set(id);
  }

  public static Integer getPageNumber() {
    return pageNumber.get() != null ? pageNumber.get() : 1; // 기본값 1
  }

  public static void setPageNumber(Integer number) {
    pageNumber.set(number);
  }

  public static void clear() {
    guardianId.remove();
    pageNumber.remove();
    pageNumber.remove();
  }
}
