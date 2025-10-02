package com.example.NoteKeep.Model;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


import java.time.Instant;
import java.util.List;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "notes")
public class Note {

    @Id
    private String id;

    private String title;

    private String content;

    private String userId;

    //private List<String> categoryIds;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;






}