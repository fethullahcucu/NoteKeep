FROM eclipse-temurin:17-jre
WORKDIR /app
COPY target/NoteKeep-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
COPY . .
ENTRYPOINT ["/app/entrypoint.sh"]