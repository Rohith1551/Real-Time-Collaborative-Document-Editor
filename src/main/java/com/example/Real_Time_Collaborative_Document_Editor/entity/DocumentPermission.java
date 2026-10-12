package com.example.Real_Time_Collaborative_Document_Editor.entity;

import com.example.Real_Time_Collaborative_Document_Editor.enums.Role;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "document_permissions",
       indexes = {
        @Index(
                name = "idx_document_permission_document_user",
                columnList = "document_id,user_id"
        )
        }
)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DocumentPermission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id")
    private Document document;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    private Role role;
}
