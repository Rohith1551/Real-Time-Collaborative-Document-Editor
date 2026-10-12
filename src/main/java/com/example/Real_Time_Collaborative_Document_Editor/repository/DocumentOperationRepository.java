package com.example.Real_Time_Collaborative_Document_Editor.repository;

import com.example.Real_Time_Collaborative_Document_Editor.dto.DocumentOperationTransformData;
import com.example.Real_Time_Collaborative_Document_Editor.entity.DocumentOperation;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DocumentOperationRepository extends JpaRepository<DocumentOperation,Long> {
    @EntityGraph(attributePaths = "user")
    List<DocumentOperation> findByDocumentIdOrderByVersionAsc(Long documentId);
    @Query("""
        SELECT new com.example.Real_Time_Collaborative_Document_Editor.dto.DocumentOperationTransformData(
            o.operation,
            o.position,
            o.text,
            o.version
        )
        FROM DocumentOperation o
        WHERE o.document.id = :documentId
        AND o.version > :version
        ORDER BY o.version ASC
        """)
    List<DocumentOperationTransformData> findOperationsForTransformation(
            @Param("documentId") Long documentId,
            @Param("version") Long version
    );


}
