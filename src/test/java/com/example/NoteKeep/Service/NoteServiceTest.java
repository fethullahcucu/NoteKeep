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
    void getAllNotesDelegatesToRepository() {
        Note first = new Note();
        Note second = new Note();
        when(noteRepository.findAll()).thenReturn(List.of(first, second));

        List<Note> notes = noteService.getAllNotes();

        assertThat(notes).containsExactly(first, second);
        verify(noteRepository).findAll();
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
    void searchAllNotesUsesRepositoryWhenKeywordProvided() {
        Note note = new Note();
        when(noteRepository.searchByKeyword("term")).thenReturn(List.of(note));

        List<Note> results = noteService.searchAllNotes(" term ");

        assertThat(results).containsExactly(note);
        verify(noteRepository).searchByKeyword("term");
    }

    @Test
    void searchAllNotesFallsBackToAllNotesWhenKeywordBlank() {
        Note note = new Note();
        when(noteRepository.findAll()).thenReturn(List.of(note));

        List<Note> results = noteService.searchAllNotes("   ");

        assertThat(results).containsExactly(note);
        verify(noteRepository).findAll();
        verifyNoMoreInteractions(noteRepository);
    }

    @Test
    void getNoteByIdReturnsEmptyWhenIdBlank() {
        Optional<Note> note = noteService.getNoteById("   ");

        assertThat(note).isEmpty();
        verifyNoInteractions(noteRepository);
    }

    @Test
    void getNoteByIdDelegatesWhenIdPresent() {
        Note existing = new Note();
        when(noteRepository.findById("note-1")).thenReturn(Optional.of(existing));

        Optional<Note> note = noteService.getNoteById("note-1");

        assertThat(note).contains(existing);
        verify(noteRepository).findById("note-1");
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
    void updateNoteAsAdminUpdatesAnyExistingNote() {
        Note existing = new Note();
        existing.setId("note-1");
        existing.setUserId("someone-else");
        when(noteRepository.findById("note-1")).thenReturn(Optional.of(existing));

        boolean updated = noteService.updateNoteAsAdmin("note-1", "  Admin Title  ", "  Admin Body  ");

        assertThat(updated).isTrue();
        ArgumentCaptor<Note> captor = ArgumentCaptor.forClass(Note.class);
        verify(noteRepository).save(captor.capture());
        Note saved = captor.getValue();
        assertThat(saved.getUserId()).isEqualTo("someone-else");
        assertThat(saved.getTitle()).isEqualTo("Admin Title");
        assertThat(saved.getContent()).isEqualTo("Admin Body");
    }

    @Test
    void updateNoteAsAdminReturnsFalseWhenNotFound() {
        when(noteRepository.findById("missing")).thenReturn(Optional.empty());

        boolean updated = noteService.updateNoteAsAdmin("missing", "Title", "Body");

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
