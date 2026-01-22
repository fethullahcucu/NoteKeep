FROM eclipse-temurin:17-jre
LABEL org.opencontainers.image.source https://github.com/fethullahcucu/notekeep
WORKDIR /app
COPY target/NoteKeep-0.0.1-SNAPSHOT.jar app.jar
COPY . .
COPY entrypoint.sh /app/entrypoint.sh
RUN chmod +x /app/entrypoint.sh
EXPOSE 8080
ENTRYPOINT ["/app/entrypoint.sh"]
