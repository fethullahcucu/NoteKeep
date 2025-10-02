package com.example.NoteKeep.Service;

import com.example.NoteKeep.Model.Note;
import com.example.NoteKeep.Repository.NoteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NoteServiceTest {

    @Mock
    private NoteRepository noteRepository;

    @InjectMocks
    private NoteService noteService;

    @Test
    void getNotesForUserReturnsEmptyListWhenUserIdBlank() {
        List<Note> notes = noteService.getNotesForUser("   ");

        assertThat(notes).isEmpty();
        verifyNoInteractions(noteRepository);
    }

    @Test
    void getNotesForUserDelegatesToRepositoryWhenUserIdPresent() {
        Note note = new Note();
        note.setUserId("user-1");
        when(noteRepository.findByUserId("user-1")).thenReturn(List.of(note));

        List<Note> notes = noteService.getNotesForUser("user-1");

        assertThat(notes).containsExactly(note);
        verify(noteRepository).findByUserId("user-1");
    }

    @Test
    void addNotePersistsWhenUserIdPresent() {
        Note note = new Note();
        note.setUserId("user-123");

        noteService.addNote(note);

        ArgumentCaptor<Note> captor = ArgumentCaptor.forClass(Note.class);
        verify(noteRepository).save(captor.capture());
        assertThat(captor.getValue().getUserId()).isEqualTo("user-123");
    }

    @Test
    void addNoteDoesNothingWhenUserIdMissing() {
        noteService.addNote(new Note());

        verifyNoInteractions(noteRepository);
    }

    @Test
    void searchNotesUsesRepositoryWhenKeywordProvided() {
        Note note = new Note();
        when(noteRepository.searchByUserIdAndKeyword("user-1", "term")).thenReturn(List.of(note));

        List<Note> results = noteService.searchNotes(" term ", "user-1");

        assertThat(results).containsExactly(note);
        verify(noteRepository).searchByUserIdAndKeyword("user-1", "term");
    }

    @Test
    void searchNotesFallsBackToAllNotesWhenKeywordBlank() {
        Note note = new Note();
        when(noteRepository.findByUserId("user-1")).thenReturn(List.of(note));

        List<Note> results = noteService.searchNotes("   ", "user-1");

        assertThat(results).containsExactly(note);
        verify(noteRepository).findByUserId("user-1");
        verifyNoMoreInteractions(noteRepository);
    }

    @Test
    void updateNoteForUserTrimsFieldsAndSaves() {
        Note existing = new Note();
        existing.setId("note-1");
        existing.setUserId("user-1");
        when(noteRepository.findById("note-1")).thenReturn(Optional.of(existing));

        boolean updated = noteService.updateNoteForUser("note-1", "user-1", "  Title  ", "  Body  ");

        assertThat(updated).isTrue();
        ArgumentCaptor<Note> captor = ArgumentCaptor.forClass(Note.class);
        verify(noteRepository).save(captor.capture());
        Note saved = captor.getValue();
        assertThat(saved.getTitle()).isEqualTo("Title");
        assertThat(saved.getContent()).isEqualTo("Body");
    }

    @Test
    void updateNoteForUserReturnsFalseWhenNotFound() {
        when(noteRepository.findById("missing")).thenReturn(Optional.empty());

        boolean updated = noteService.updateNoteForUser("missing", "user-1", "Title", "Body");

        assertThat(updated).isFalse();
        verify(noteRepository, never()).save(any());
    }

    @Test
    void deleteNoteForUserRemovesWhenOwned() {
        Note existing = new Note();
        existing.setId("note-1");
        existing.setUserId("user-1");
        when(noteRepository.findById("note-1")).thenReturn(Optional.of(existing));

        boolean deleted = noteService.deleteNoteForUser("note-1", "user-1");

        assertThat(deleted).isTrue();
        verify(noteRepository).deleteById("note-1");
    }

    @Test
    void deleteNoteForUserReturnsFalseWhenNotOwned() {
        Note existing = new Note();
        existing.setId("note-1");
        existing.setUserId("someone-else");
        when(noteRepository.findById("note-1")).thenReturn(Optional.of(existing));

        boolean deleted = noteService.deleteNoteForUser("note-1", "user-1");

        assertThat(deleted).isFalse();
        verify(noteRepository, never()).deleteById(any());
    }
}
