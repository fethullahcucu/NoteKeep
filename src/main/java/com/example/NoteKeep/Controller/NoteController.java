package com.example.NoteKeep.Controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.NoteKeep.Model.Note;
import com.example.NoteKeep.Service.NoteService;
import com.example.UserRegistration.Dto.UserDto;
import com.example.UserRegistration.Model.Role;
import com.example.UserRegistration.Service.UserService;

@Controller
@RequestMapping("/notes")
public class NoteController {

    private static final Logger logger = LoggerFactory.getLogger(NoteController.class);

    @Autowired
    private NoteService noteService;

    @Autowired
    private UserService userService;

    //1@Value("${notekeep.ocr.enabled:true}")
    //1private boolean ocrEnabled;

    @GetMapping
    public String getNotes(@RequestParam(value = "search", required = false) String search,
                           @RequestParam(value = "tags", required = false) String tagsParam,
                           @RequestParam(value = "filterUserId", required = false) String filterUserId,
                           Model model, Authentication authentication) {
        List<Note> notes = new ArrayList<>();
        String email = authentication.getName();
        com.example.UserRegistration.Model.User user = userService.findUserByEmail(email);
        boolean admin = isAdmin(user);

        if (user != null && user.getId() != null) {
            if (admin && filterUserId != null && !filterUserId.isBlank()) {
                if (search != null && !search.isBlank()) {
                    notes = noteService.searchNotes(search, filterUserId);
                } else {
                    notes = noteService.getNotesForUser(filterUserId);
                }
            } else if (admin && search != null && !search.isBlank()) {
                notes = noteService.searchAllNotes(search);
            } else if (admin) {
                notes = noteService.getAllNotes();
            } else if (search != null && !search.isBlank()) {
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

        Map<String, com.example.UserRegistration.Model.User> userMap = new HashMap<>();
        List<UserDto> allUsers = new ArrayList<>();
        if (admin) {
            for (Note note : notes) {
                if (note.getUserId() != null && !userMap.containsKey(note.getUserId())) {
                    com.example.UserRegistration.Model.User noteUser = userService.findUserById(note.getUserId());
                    if (noteUser != null) {
                        userMap.put(note.getUserId(), noteUser);
                    }
                }
            }
            allUsers = userService.findAllUsers();
        }

        model.addAttribute("notes", notes);
        model.addAttribute("search", search);
        model.addAttribute("hashtags", hashtags);
        model.addAttribute("selectedTags", selectedTags);
        model.addAttribute("isAdmin", admin);
        model.addAttribute("currentUserId", user == null ? null : user.getId());
        model.addAttribute("userMap", userMap);
        model.addAttribute("allUsers", allUsers);
        model.addAttribute("selectedUserId", filterUserId);
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
        if (isAdmin(user)) {
            logger.warn("Admin user {} attempted to create a note", email);
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

        boolean admin = isAdmin(user);
        Optional<Note> note = admin
                ? noteService.getNoteById(noteId)
                : noteService.getNoteByIdForUser(noteId, user.getId());
        if (note.isEmpty()) {
            logger.warn("Edit denied: note not found or does not belong to user. noteId={}, userId={}", noteId, user.getId());
            return "redirect:/notes";
        }

        model.addAttribute("note", note.get());
        model.addAttribute("isAdmin", admin);
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

        boolean admin = isAdmin(user);
        boolean updated = admin
                ? noteService.updateNoteAsAdmin(noteId, title, content)
                : noteService.updateNoteForUser(noteId, user.getId(), title, content);
        if (!updated) {
            logger.warn("Update denied: note not found or does not belong to user. noteId={}, userId={}", noteId, user.getId());
        } else {
            logger.info("Note updated: id={} by user={} admin={}", noteId, user.getId(), admin);
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

    private boolean isAdmin(com.example.UserRegistration.Model.User user) {
        return user != null && user.getRoles() != null && user.getRoles().contains(Role.ROLE_ADMIN);
    }
    /* 
    @PostMapping(value = "/detect")
    @ResponseBody
    public String detectTextFromImage(@RequestParam("file") MultipartFile file, Authentication authentication) {
        //1 if (!ocrEnabled) {
        //1     logger.info("OCR disabled; skipping text detection");
        //1     return "";
        //1 }
        if (file == null || file.isEmpty()) return "";
        try {
            byte[] bytes = file.getBytes();
            String detected = DetectText.detectTextFromBytes(bytes);
            return detected != null ? detected : "";
        } catch (IOException e) {
            logger.error("Text detection failed", e);
            return "";
        } 
    } */
}
