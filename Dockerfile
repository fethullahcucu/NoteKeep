FROM eclipse-temurin:17-jre
LABEL org.opencontainers.image.source = https://github.com/fethullahcucu/notekeep #Replace OWNER and REPO with your actual GitHub info
WORKDIR /app
COPY target/NoteKeep-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
COPY . .
ENTRYPOINT ["/app/entrypoint.sh"]