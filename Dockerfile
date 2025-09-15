ARG BUILDER_IMAGE=gradle:7.6.0-jdk17
ARG RUNTIME_IMAGE=amazoncorretto:17.0.7-alpine

# =================== builder ===================
FROM ${BUILDER_IMAGE} AS builder
WORKDIR /app
ENV GRADLE_USER_HOME=/home/gradle/.gradle

USER root
# making director if non-existent
RUN mkdir -p ${GRADLE_USER_HOME} && chown -R gradle:gradle /home/gradle /app

USER gradle
# gradlew - executable file
COPY --chown=gradle:gradle gradlew ./
# gradle - gradle wrapper directory
COPY --chown=gradle:gradle gradle ./gradle
# other related files: settings.gradle, build.gradle
COPY --chown=gradle:gradle settings.gradle build.gradle ./

# downloading dependencies in container
RUN chmod +x ./gradlew
RUN ./gradlew --no-daemon dependencies || true

# moving MY src to container's src
COPY --chown=gradle:gradle src ./src
RUN ./gradlew clean build --no-daemon --no-parallel -x test

# verification
RUN ls -l /app/build/libs

# =================== runtime ===================
FROM ${RUNTIME_IMAGE}
WORKDIR /app
ENV SPRING_PROFILES_ACTIVE=prod
ENV JVM_OPTS=""

# copying only the built .jar file (in previous build stage - builder) to final image
COPY --from=builder /app/build/libs/*.jar app.jar
EXPOSE 80
#ENTRYPOINT ["sh", "-c", "java $JVM_OPTS -jar app.jar"]
ENTRYPOINT ["java", "-jar", "app.jar"]

