package com.example.Real_Time_Collaborative_Document_Editor.repository;

import com.example.Real_Time_Collaborative_Document_Editor.dto.DocumentResponse;
import com.example.Real_Time_Collaborative_Document_Editor.dto.DocumentSummaryResponse;
import com.example.Real_Time_Collaborative_Document_Editor.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {

    Optional<Document> findByIdAndOwnerEmail(Long id, String email);

    @Query("""
        SELECT new com.example.Real_Time_Collaborative_Document_Editor.dto.DocumentSummaryResponse(
            d.id,
            d.title,
            d.content,
            d.version
        )
        FROM Document d
        """)
    List<DocumentSummaryResponse> findAllDocumentSummaries();

    @Query("""
        SELECT new com.example.Real_Time_Collaborative_Document_Editor.dto.DocumentResponse(
            d.id,
            d.title,
            d.content,
            d.version
        )
        FROM Document d
        WHERE d.id = :id
        """)
    Optional<DocumentResponse> findDocumentResponseById(
            @Param("id") Long id
    );

}