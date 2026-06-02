FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/brevery-*.jar app.jar
EXPOSE 8080
ENV JAVA_OPTS="-Xmx512m -Xms256m"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
