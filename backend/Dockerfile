FROM eclipse-temurin:25-jdk AS build
WORKDIR /src
COPY .mvn .mvn
COPY mvnw pom.xml ./
COPY backend backend
COPY config-server/pom.xml config-server/pom.xml
RUN chmod +x mvnw && MAVEN_OPTS="-Xmx384m" ./mvnw -B -ntp -pl backend -am package -DskipTests

FROM eclipse-temurin:25-jre
WORKDIR /app
RUN useradd --system --uid 10001 movimento && mkdir data && chown movimento data
COPY --from=build /src/backend/target/movimento-*.jar app.jar
ENV SERVER_ADDRESS=0.0.0.0
ENV JAVA_TOOL_OPTIONS="-Xms32m -Xmx256m -XX:MaxMetaspaceSize=160m -XX:ActiveProcessorCount=2 -XX:+ExitOnOutOfMemoryError"
USER movimento
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
