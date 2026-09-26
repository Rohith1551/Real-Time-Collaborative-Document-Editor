package com.example.Real_Time_Collaborative_Document_Editor.repository;

import com.example.Real_Time_Collaborative_Document_Editor.entity.DocumentVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.print.Doc;
import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentVersionRepository extends JpaRepository<DocumentVersion, Long> {

    List<DocumentVersion> findByDocumentIdOrderByCreatedAtDesc(Long documentId);

    Optional<DocumentVersion> findByIdAndDocumentId(Long id, Long DocumentId);

    boolean existsByDocumentIdAndTitleAndContent(Long DocumentId, String title, String content);
}
