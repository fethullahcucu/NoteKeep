package com.example.NoteKeep.Service;

import com.example.NoteKeep.Model.Note;
import com.example.NoteKeep.Repository.NoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class NoteService {

    @Autowired
    private NoteRepository noteRepository;

    public List<Note> getNotesForUser(String userId) {
        if (userId == null || userId.isBlank()) {
            return Collections.emptyList();
        }
        return noteRepository.findByUserId(userId);
    }

    public List<Note> searchNotes(String keyword, String userId) {
        if (userId == null || userId.isBlank()) {
            return Collections.emptyList();
        }
        if (keyword == null || keyword.isBlank()) {
            return getNotesForUser(userId);
        }
        return noteRepository.searchByUserIdAndKeyword(userId, keyword.trim());
    }

    public void addNote(Note note) {
        if (note == null || note.getUserId() == null || note.getUserId().isBlank()) {
            return;
        }
        noteRepository.save(note);
    }

    public Optional<Note> getNoteByIdForUser(String noteId, String userId) {
        if (noteId == null || noteId.isBlank() || userId == null || userId.isBlank()) {
            return Optional.empty();
        }
        return noteRepository.findById(noteId)
                .filter(note -> userId.equals(note.getUserId()));
    }

    public boolean deleteNoteForUser(String noteId, String userId) {
        Optional<Note> noteOpt = getNoteByIdForUser(noteId, userId);
        if (noteOpt.isEmpty()) {
            return false;
        }
        noteRepository.deleteById(noteId);
        return true;
    }

    public boolean updateNoteForUser(String noteId, String userId, String title, String content) {
        if (noteId == null || noteId.isBlank() || userId == null || userId.isBlank()) {
            return false;
        }

        Optional<Note> noteOpt = getNoteByIdForUser(noteId, userId);
        if (noteOpt.isEmpty()) {
            return false;
        }

        Note note = noteOpt.get();
        if (title != null) {
            note.setTitle(title.trim());
        }
        if (content != null) {
            note.setContent(content.trim());
        }
        noteRepository.save(note);
        return true;
    }
}
