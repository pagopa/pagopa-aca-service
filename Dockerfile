FROM amazoncorretto:17-alpine@sha256:1b1d0653d890ff313a1f7afadd1fd81f5ea742c9c48670d483b1bbccef98bb8b AS build
WORKDIR /workspace/app

COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .
COPY gradle.lockfile .
COPY gradle.properties .

COPY eclipse-style.xml eclipse-style.xml
COPY src src
COPY api-spec api-spec
RUN ./gradlew build -x test
RUN mkdir build/extracted && java -Djarmode=layertools -jar build/libs/*.jar extract --destination build/extracted

FROM amazoncorretto:17-alpine@sha256:1b1d0653d890ff313a1f7afadd1fd81f5ea742c9c48670d483b1bbccef98bb8b

RUN addgroup --system user && adduser --ingroup user --system user
USER user:user

WORKDIR /app/

ARG EXTRACTED=/workspace/app/build/extracted

ADD --chown=user https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v1.25.1/opentelemetry-javaagent.jar .

COPY --from=build --chown=user ${EXTRACTED}/dependencies/ ./
RUN true
COPY --from=build --chown=user ${EXTRACTED}/spring-boot-loader/ ./
RUN true
COPY --from=build --chown=user ${EXTRACTED}/snapshot-dependencies/ ./
RUN true
COPY --from=build --chown=user ${EXTRACTED}/application/ ./
RUN true

ENTRYPOINT ["java","--enable-preview","-javaagent:opentelemetry-javaagent.jar","org.springframework.boot.loader.JarLauncher"]

