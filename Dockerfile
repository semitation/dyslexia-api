
FROM openjdk:17-slim

WORKDIR /app

COPY app.jar /app/app.jar

EXPOSE 8080

CMD ["java", "-jar", "/app/app.jar"]