package com.example.Real_Time_Collaborative_Document_Editor.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "documents")
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @ManyToOne
    @JoinColumn(name = "owner_id",nullable = false)
    private User owner;

    @OneToMany(mappedBy = "document" , cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DocumentPermission> permissions;

    @OneToMany(mappedBy = "document" , cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DocumentVersion> versions;

}
