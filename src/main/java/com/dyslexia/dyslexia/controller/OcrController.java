package com.dyslexia.dyslexia.controller;


import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/ocr")
public class OcrController {
  @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<String> upload(@RequestParam("file") MultipartFile file)
      throws IOException, TesseractException {
    File tempPdf = File.createTempFile("upload", ".pdf");
    file.transferTo(tempPdf);

    PDDocument document = PDDocument.load(tempPdf);
    PDFRenderer renderer = new PDFRenderer(document);
    StringBuilder extractedText = new StringBuilder();

    Tesseract tesseract = new Tesseract();

    String os = System.getProperty("os.name").toLowerCase();

    if (os.contains("win")) {
      // Windows 환경
      tesseract.setDatapath("C:\\Program Files\\Tesseract-OCR\\tessdata");
    } else {
      // Ubuntu 환경
      tesseract.setDatapath("/usr/share/tesseract-ocr/5.5.0/tessdata");
    }

    tesseract.setLanguage("kor+eng");

    StringBuilder resultText = new StringBuilder();

    for (int i = 0; i < document.getNumberOfPages(); i++) {
      BufferedImage image = renderer.renderImageWithDPI(i, 300);

      String text = tesseract.doOCR(image);
      resultText.append(text).append("\n");
    }

    document.close();
    tempPdf.delete();

    return ResponseEntity.ok(resultText.toString());

  }
}
