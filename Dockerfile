FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

RUN apk add --no-cache curl

# 빌드 인수로 캐시 무효화
ARG COMMIT_SHA
ARG BUILD_TIME
ARG FORCE_REBUILD
ENV COMMIT_SHA=${COMMIT_SHA}
ENV BUILD_TIME=${BUILD_TIME}
ENV FORCE_REBUILD=${FORCE_REBUILD}

# Gradle wrapper와 빌드 스크립트만 먼저 복사
COPY gradlew .
COPY gradle/ gradle/
COPY build.gradle .
COPY settings.gradle .

RUN chmod +x gradlew

# 의존성 다운로드 (캐시 레이어 생성)
RUN ./gradlew dependencies --no-daemon --refresh-dependencies || true

# 전체 소스코드 복사
COPY . .

# 빌드 정보 출력 및 기존 빌드 결과 정리
RUN echo "Force rebuild: ${FORCE_REBUILD}" && \
    echo "Building with COMMIT_SHA: ${COMMIT_SHA}" && \
    echo "Build time: ${BUILD_TIME}" && \
    rm -rf build/ .gradle/caches/build-cache-* || true

# 완전한 클린 빌드 (최신 의존성)
RUN ./gradlew clean build -x test \
    --no-daemon \
    --no-build-cache \
    --refresh-dependencies \
    --rerun-tasks \
    --max-workers=4 \
    --stacktrace \
    --info

# 최종 실행 이미지
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

RUN apk add --no-cache curl tzdata

# 타임존 설정
ENV TZ=Asia/Seoul

# 비root 사용자 생성
RUN addgroup -g 1001 -S batch && adduser -u 1001 -S batch -G batch

# JAR 파일 복사 (root로 복사 후 권한 변경)
COPY --from=builder /app/build/libs/*.jar app.jar
RUN chown batch:batch app.jar

USER batch

EXPOSE 8081

# JVM 최적화 (배치용)
ENV JAVA_OPTS="-Xms512m -Xmx1g -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+UseStringDeduplication -Djava.security.egd=file:/dev/./urandom -Duser.timezone=Asia/Seoul"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

HEALTHCHECK --interval=30s --timeout=10s --start-period=90s --retries=3 \
    CMD curl -f http://localhost:8081/actuator/health || exit 1