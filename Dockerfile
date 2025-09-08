# flik-batch-server Dockerfile - ARM64
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

RUN apk add --no-cache curl

# Gradle 설정 먼저 복사
COPY gradlew .
COPY gradle gradle/
COPY build.gradle settings.gradle ./

RUN chmod +x gradlew

# 의존성 다운로드
RUN ./gradlew dependencies --no-daemon --parallel --max-workers=4

# 소스 코드 복사
COPY src src/

# 빌드 실행
RUN ./gradlew clean build -x test --no-daemon --parallel --max-workers=4 --build-cache

# 최종 실행 이미지
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

RUN apk add --no-cache curl

# 비root 사용자 생성
RUN addgroup -g 1001 -S batch && adduser -u 1001 -S batch -G batch
USER batch

# JAR 파일 복사
COPY --from=builder --chown=batch:batch /app/build/libs/*.jar app.jar

EXPOSE 8081

# JVM 최적화 (배치용)
ENV JAVA_OPTS="-Xms512m -Xmx1g -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+UseStringDeduplication -Djava.security.egd=file:/dev/./urandom"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8081/actuator/health || exit 1