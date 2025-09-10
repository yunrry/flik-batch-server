FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

RUN apk add --no-cache curl

# 빌드 인수
ARG COMMIT_SHA
ARG BUILD_TIME
ENV COMMIT_SHA=${COMMIT_SHA}
ENV BUILD_TIME=${BUILD_TIME}

# Gradle wrapper 복사
COPY gradlew .
COPY gradle/ gradle/
RUN chmod +x gradlew

# 의존성 설정 파일만 먼저 복사 (중요!)
COPY build.gradle .
COPY settings.gradle .

# 의존성 다운로드 (이 레이어는 build.gradle이 변경될 때만 재빌드)
RUN ./gradlew dependencies --no-daemon --refresh-dependencies

# 소스코드는 나중에 복사 (소스 변경 시 의존성 레이어는 캐시 사용)
COPY src/ src/

# 빌드 (테스트 제외, 캐시 활용)
RUN ./gradlew clean bootJar -x test \
    --no-daemon \
    --parallel \
    --build-cache

# 실행 이미지
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

RUN apk add --no-cache curl tzdata && \
    addgroup -g 1001 -S batch && \
    adduser -u 1001 -S batch -G batch

# JAR 파일 복사
COPY --from=builder --chown=batch:batch /app/build/libs/*.jar app.jar

USER batch

EXPOSE 8081

# JVM 최적화
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -Duser.timezone=Asia/Seoul"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8081/actuator/health || exit 1