package com.example.Real_Time_Collaborative_Document_Editor.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DocumentOperation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long documentId;

    private String email;

    private String operation;

    private Integer position;

    @Column(columnDefinition = "TEXT")
    private String text;

    private Long version;

    private LocalDateTime createdAt;
}