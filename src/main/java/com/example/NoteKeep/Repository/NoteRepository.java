package com.example.NoteKeep.Repository;

import com.example.NoteKeep.Model.Note;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface NoteRepository extends MongoRepository<Note, String> {
    List<Note> findByUserId(String userId);

    @Query("{ 'userId': ?0, $or: [ { 'title': { $regex: ?1, $options: 'i' } }, { 'content': { $regex: ?1, $options: 'i' } } ] }")
    List<Note> searchByUserIdAndKeyword(String userId, String keyword);
}
