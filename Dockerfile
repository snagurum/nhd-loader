#FROM maven:3.8.3-jdk-17 AS build
FROM maven:3.8.3-openjdk-17 AS build
COPY . /app
WORKDIR /app
RUN mvn package -DskipTests
RUN mv /app/target/nhd*.jar /app/target/nhd.jar

# Second stage: create a slim image
FROM openjdk:17
COPY --from=build /app/target/nhd.jar /nhd.jar
ENV TZ="Asia/Kolkata"
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/nhd.jar"]
