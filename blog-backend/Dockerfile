FROM eclipse-temurin:17-jdk-alpine as builder

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN apk add --no-cache maven && \
    mvn clean package -DskipTests && \
    mv target/*.jar app.jar

FROM eclipse-temurin:17-jre-alpine

RUN apk add --no-cache tzdata freetype fontconfig && \
    cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    echo "Asia/Shanghai" > /etc/timezone

WORKDIR /app

RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

RUN mkdir -p /var/www/blog/uploads && \
    chown -R appuser:appgroup /var/www/blog/uploads

COPY --from=builder /app/app.jar .

USER appuser

EXPOSE 8080

ENTRYPOINT ["java", "-Djava.awt.headless=true", "-Xmx768m", "-Xms256m", "-jar", "app.jar"]
