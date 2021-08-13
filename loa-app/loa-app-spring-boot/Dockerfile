FROM adoptopenjdk/openjdk11:jdk-11.0.9_11-alpine-slim
RUN addgroup -S spring && adduser -S spring -G spring
RUN mkdir /mnt/spring  && chown spring /mnt/spring
USER spring:spring
VOLUME /mnt/spring
VOLUME /tmp
ARG DEPENDENCY=target/dependency
COPY ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY ${DEPENDENCY}/META-INF /app/META-INF
COPY ${DEPENDENCY}/BOOT-INF/classes /app
ENTRYPOINT ["java","-cp","app:app/lib/*","org.linkedopenactors.code.loaapp.LoaAppApplication"]
