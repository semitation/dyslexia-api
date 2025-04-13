
FROM openjdk:17-slim

# chatgpt 에서 알려준 테서렉트 설치 코드
RUN apt-get update && apt-get install -y \
    tesseract-ocr \
    tesseract-ocr-kor \
    tesseract-ocr-eng \
    libtesseract-dev \
    && apt-get clean \

WORKDIR /app

COPY app.jar /app/app.jar

EXPOSE 8080

CMD ["java", "-jar", "/app/app.jar"]