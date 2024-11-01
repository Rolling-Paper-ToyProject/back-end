# Build stage
FROM gradle:jdk21 AS builder
WORKDIR /build
COPY . .
RUN gradle build -x test --no-daemon

# Runtime stage
FROM openjdk:21-jdk-slim
WORKDIR /app

# 타임존 설정
ENV TZ=Asia/Seoul
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 빌드 스테이지에서 생성된 JAR 파일만 복사
COPY --from=builder /build/build/libs/sparkle-note-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", \
            "-Duser.timezone=Asia/Seoul", \
            "-XX:+UseContainerSupport", \
            "-XX:MaxRAMPercentage=75.0", \
            "/app.jar"]