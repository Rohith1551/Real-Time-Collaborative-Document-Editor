package com.example.Real_Time_Collaborative_Document_Editor.repository;


import com.example.Real_Time_Collaborative_Document_Editor.entity.Document;
import com.example.Real_Time_Collaborative_Document_Editor.entity.DocumentPermission;
import com.example.Real_Time_Collaborative_Document_Editor.entity.User;
import com.example.Real_Time_Collaborative_Document_Editor.enums.Role;
import jakarta.persistence.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DocumentPermissionRepository extends JpaRepository<DocumentPermission,Long> {


    Optional<DocumentPermission> findByDocumentIdAndUserId(Long documentId, Long userId);
}
