package com.example.Real_Time_Collaborative_Document_Editor.repository;

import com.example.Real_Time_Collaborative_Document_Editor.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DocumentRepository extends JpaRepository<Document,Long> {

    Optional<Document> findByIdAndOwnerEmail(Long id, String email);
}
