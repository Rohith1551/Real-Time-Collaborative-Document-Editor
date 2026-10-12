package com.example.Real_Time_Collaborative_Document_Editor.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "document_operation",
        indexes = {
                @Index(
                        name = "idx_document_operation_document_version",
                        columnList = "document_id, version"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DocumentOperation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    @ManyToOne
    @JoinColumn(name = "user_id",nullable = false)
    private User user;

    @Column(nullable = false)
    private String operation;

    private Integer position;

    @Column(columnDefinition = "TEXT")
    private String text;

    @Column(nullable = false)
    private Long version;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}