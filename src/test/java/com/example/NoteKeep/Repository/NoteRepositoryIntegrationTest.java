package com.example.NoteKeep.Repository;

import com.example.NoteKeep.Model.Note;
import com.mongodb.MongoException;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class NoteRepositoryIntegrationTest {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private NoteRepository noteRepository;

    @Test
    void mongodbConnectionAllowsRepositoryOperations() {
        Assumptions.assumeTrue(canReachMongo(), "MongoDB instance not reachable at configured URI");

        String userId = "connection-test-" + UUID.randomUUID();

        Note note = new Note();
        note.setTitle("Connection check");
        note.setContent("Testing Mongo connection");
        note.setUserId(userId);

        noteRepository.save(note);

        try {
            List<Note> fetched = noteRepository.findByUserId(userId);
            assertThat(fetched).hasSize(1);
            assertThat(fetched.get(0).getContent()).isEqualTo("Testing Mongo connection");
        } finally {
            noteRepository.deleteAll(noteRepository.findByUserId(userId));
        }
    }

    private boolean canReachMongo() {
        try {
            mongoTemplate.executeCommand("{ ping: 1 }");
            return true;
        } catch (MongoException | DataAccessException ex) {
            return false;
        }
    }
}
