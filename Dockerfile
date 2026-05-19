FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S petbuddies && adduser -S petbuddies -G petbuddies
COPY target/*.jar app.jar
USER petbuddies
EXPOSE 8080
ENTRYPOINT ["java", "-Duser.timezone=America/Sao_Paulo", "-jar", "app.jar"]
