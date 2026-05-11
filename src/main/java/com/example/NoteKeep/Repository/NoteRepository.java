package com.example.NoteKeep.Repository;

import com.example.NoteKeep.Model.Note;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface NoteRepository extends MongoRepository<Note, String> {
    List<Note> findByUserId(String userId);

    @Query("{ 'userId': ?0, $or: [ { 'title': { $regex: ?1, $options: 'i' } }, { 'content': { $regex: ?1, $options: 'i' } } ] }")
    List<Note> searchByUserIdAndKeyword(String userId, String keyword);

    @Query("{ $or: [ { 'title': { $regex: ?0, $options: 'i' } }, { 'content': { $regex: ?0, $options: 'i' } }, { 'userId': { $regex: ?0, $options: 'i' } } ] }")
    List<Note> searchByKeyword(String keyword);
}
