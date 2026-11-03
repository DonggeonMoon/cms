# syntax=docker/dockerfile:1

FROM node:24-alpine AS frontend
WORKDIR /frontend
COPY frontend-react/package.json frontend-react/package-lock.json ./
RUN --mount=type=cache,target=/root/.npm npm ci
COPY frontend-react/ ./
RUN npm run build

FROM eclipse-temurin:21-jdk AS backend
WORKDIR /workspace
COPY gradlew settings.gradle build.gradle ./
COPY gradle gradle
RUN --mount=type=cache,target=/root/.gradle ./gradlew --no-daemon dependencies > /dev/null
COPY src src
COPY --from=frontend /frontend/dist src/main/resources/static
RUN --mount=type=cache,target=/root/.gradle ./gradlew --no-daemon bootJar \
    && find build/libs -name '*.jar' ! -name '*-plain.jar' -exec cp {} /workspace/app.jar \;

FROM eclipse-temurin:21-jre
RUN groupadd --system cms && useradd --system --gid cms --home-dir /app cms \
    && mkdir -p /app /data/uploads && chown -R cms:cms /app /data
WORKDIR /app
COPY --from=backend /workspace/app.jar app.jar
USER cms
ENV CMS_UPLOAD_DIR=/data/uploads \
    JAVA_OPTS="-XX:MaxRAMPercentage=75"
VOLUME /data/uploads
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
