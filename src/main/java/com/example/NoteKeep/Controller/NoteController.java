package com.example.NoteKeep.Controller;

import com.example.NoteKeep.Model.Note;
import com.example.NoteKeep.Service.NoteService;
import com.example.UserRegistration.Service.UserService;
import com.example.NoteKeep.DetectText;
import org.springframework.security.core.Authentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;

import java.util.Set;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/notes")
public class NoteController {

    private static final Logger logger = LoggerFactory.getLogger(NoteController.class);

    @Autowired
    private NoteService noteService;

    @Autowired
    private UserService userService;

    @GetMapping
    public String getNotes(@RequestParam(value = "search", required = false) String search,
                           @RequestParam(value = "tags", required = false) String tagsParam,
                           Model model, Authentication authentication) {
        List<Note> notes = new ArrayList<>();
        String email = authentication.getName();
        com.example.UserRegistration.Model.User user = userService.findUserByEmail(email);

        if (user != null && user.getId() != null) {
            if (search != null && !search.isBlank()) {
                notes = noteService.searchNotes(search, user.getId());
            } else {
                notes = noteService.getNotesForUser(user.getId());
            }
        } else {
            logger.warn("Authenticated principal {} does not have a linked user record", email);
        }

        Set<String> hashtags = new LinkedHashSet<>();
        Pattern pattern = Pattern.compile("#\\w+");
        for (Note note : notes) {
            if (note.getContent() == null) continue;
            Matcher m = pattern.matcher(note.getContent());
            while (m.find()) {
                hashtags.add(m.group());
            }
        }

        Set<String> selectedTags = new LinkedHashSet<>();
        if (tagsParam != null && !tagsParam.isBlank()) {
            for (String t : tagsParam.split(",")) {
                if (!t.isBlank()) selectedTags.add(t.trim());
            }
        }

        if (!selectedTags.isEmpty()) {
            final Set<String> lowered = selectedTags.stream()
                    .map(String::toLowerCase)
                    .collect(Collectors.toSet());
            notes = notes.stream()
                    .filter(n -> {
                        String content = n.getContent() == null ? "" : n.getContent().toLowerCase();
                        return lowered.stream().allMatch(content::contains);
                    })
                    .collect(Collectors.toList());
        }

        model.addAttribute("notes", notes);
        model.addAttribute("search", search);
        model.addAttribute("hashtags", hashtags);
        model.addAttribute("selectedTags", selectedTags);
        return "notes";
    }

    @PostMapping("/add")
    public String addNote(@ModelAttribute Note note, Authentication authentication) {
        String email = authentication.getName();
        com.example.UserRegistration.Model.User user = userService.findUserByEmail(email);
        if (user == null || user.getId() == null) {
            logger.warn("Attempt to add note without a valid user; principal={} ", email);
            return "redirect:/notes";
        }
        note.setUserId(user.getId());
        noteService.addNote(note);
        return "redirect:/notes";
    }

    @GetMapping("/{noteId}/edit")
    public String editNoteForm(@PathVariable String noteId, Model model, Authentication authentication) {
        String email = authentication.getName();
        com.example.UserRegistration.Model.User user = userService.findUserByEmail(email);
        if (user == null || user.getId() == null) {
            logger.warn("Edit denied: no user record for principal={}", email);
            return "redirect:/notes";
        }

        Optional<Note> note = noteService.getNoteByIdForUser(noteId, user.getId());
        if (note.isEmpty()) {
            logger.warn("Edit denied: note not found or does not belong to user. noteId={}, userId={}", noteId, user.getId());
            return "redirect:/notes";
        }

        model.addAttribute("note", note.get());
        return "edit-note";
    }

    @PostMapping("/update")
    public String updateNote(@RequestParam String noteId,
                             @RequestParam String title,
                             @RequestParam String content,
                             Authentication authentication) {
        String email = authentication.getName();
        com.example.UserRegistration.Model.User user = userService.findUserByEmail(email);

        if (user == null || user.getId() == null) {
            logger.warn("Update denied: no user record for principal={}", email);
            return "redirect:/notes";
        }

        boolean updated = noteService.updateNoteForUser(noteId, user.getId(), title, content);
        if (!updated) {
            logger.warn("Update denied: note not found or does not belong to user. noteId={}, userId={}", noteId, user.getId());
        } else {
            logger.info("Note updated: id={} by user={}", noteId, user.getId());
        }
        return "redirect:/notes"; 
    }

    @PostMapping("/delete")
    public String deleteNote(@RequestParam String noteId, Authentication authentication) {
        String email = authentication.getName();
        com.example.UserRegistration.Model.User user = userService.findUserByEmail(email);

        if (user == null || user.getId() == null) {
            logger.warn("Delete denied: no user record for principal={}", email);
            return "redirect:/notes";
        }

        boolean deleted = noteService.deleteNoteForUser(noteId, user.getId());
        if (deleted) {
            logger.info("Note deleted: id={} by user={}", noteId, user.getId());
        } else {
            logger.warn("Delete denied: note not found or does not belong to user. noteId={}, userId={}", noteId, user.getId());
        }
        return "redirect:/notes";
    }

    @PostMapping(value = "/detect")
    @ResponseBody
    public String detectTextFromImage(@RequestParam("file") MultipartFile file, Authentication authentication) {
        if (file == null || file.isEmpty()) return "";
        try {
            byte[] bytes = file.getBytes();
            String detected = DetectText.detectTextFromBytes(bytes);
            return detected != null ? detected : "";
        } catch (IOException e) {
            logger.error("Text detection failed", e);
            return "";
        }
    }
}
