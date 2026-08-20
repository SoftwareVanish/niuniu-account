FROM eclipse-temurin:21-jre-alpine

RUN apk add --no-cache tzdata curl && \
    cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    echo "Asia/Shanghai" > /etc/timezone

WORKDIR /app

COPY niuniu_account_api/target/*.jar app.jar

RUN mkdir -p /app/logs

EXPOSE 9527

ENTRYPOINT ["java", "-jar", "-Duser.timezone=Asia/Shanghai", "app.jar"]
