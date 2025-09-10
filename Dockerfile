FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

RUN apk add --no-cache curl

# 빌드 인수로 캐시 무효화 강화
ARG COMMIT_SHA
ARG BUILD_TIME
ARG FORCE_REBUILD
ENV COMMIT_SHA=${COMMIT_SHA}
ENV BUILD_TIME=${BUILD_TIME}
ENV FORCE_REBUILD=${FORCE_REBUILD}

# 전체 프로젝트 복사
COPY . .

RUN chmod +x gradlew

# 모든 캐시 디렉터리 강제 삭제
RUN echo "Force rebuild: ${FORCE_REBUILD}" && \
    echo "Building with COMMIT_SHA: ${COMMIT_SHA}" && \
    echo "Build time: ${BUILD_TIME}" && \
    rm -rf build/ .gradle/ ~/.gradle/ /root/.gradle/ || true && \
    find . -name "*.class" -delete || true && \
    find . -name "*.jar" -delete || true

# 완전한 clean build (모든 캐시 및 점진적 빌드 비활성화)
RUN ./gradlew clean build -x test \
    --no-daemon \
    --parallel \
    --max-workers=4 \
    --no-build-cache \
    --refresh-dependencies \
    --rerun-tasks \
    --no-incremental \
    --info

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