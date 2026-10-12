package com.example.Real_Time_Collaborative_Document_Editor.controller;

import com.example.Real_Time_Collaborative_Document_Editor.dto.DocumentOperationResponse;
import com.example.Real_Time_Collaborative_Document_Editor.dto.DocumentResponse;
import com.example.Real_Time_Collaborative_Document_Editor.dto.DocumentSummaryResponse;
import com.example.Real_Time_Collaborative_Document_Editor.dto.DocumentUpdateRequest;
import com.example.Real_Time_Collaborative_Document_Editor.entity.Document;
import com.example.Real_Time_Collaborative_Document_Editor.entity.DocumentOperation;
import com.example.Real_Time_Collaborative_Document_Editor.entity.DocumentPermission;
import com.example.Real_Time_Collaborative_Document_Editor.entity.DocumentVersion;
import com.example.Real_Time_Collaborative_Document_Editor.enums.Role;
import com.example.Real_Time_Collaborative_Document_Editor.repository.DocumentOperationRepository;
import com.example.Real_Time_Collaborative_Document_Editor.repository.DocumentRepository;
import com.example.Real_Time_Collaborative_Document_Editor.repository.DocumentVersionRepository;
import com.example.Real_Time_Collaborative_Document_Editor.service.DocumentService;
import org.springframework.security.core.Authentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.print.Doc;
import java.util.List;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {
    @Autowired
    private DocumentService service;
    @Autowired
    private DocumentRepository repo;
    @Autowired
    private DocumentVersionRepository documentVersionRepository;
    @Autowired
    private DocumentOperationRepository documentOperationRepository;

    @PostMapping("/createDocument")

    public Document createDocument(@RequestBody Document document, Authentication authentication){

        return service.createDocument(document, authentication.getName());
    }

    @GetMapping("/getAllDocuments")
    public List<DocumentSummaryResponse> getAllDocument(){
        return service.getAllDocuments();
    }

    @GetMapping("/getDocument/{id}")
    public DocumentResponse getDocument(@PathVariable Long id, Authentication authentication){
        return service.getDocument(id,authentication.getName());
    }

    @PutMapping("/updateDocument/{id}")
    public DocumentResponse updateDocument(@PathVariable Long id, @RequestBody DocumentUpdateRequest updatedDocument, Authentication authentication){
        return service.updateDocument(id,updatedDocument,authentication.getName());
    }

    @DeleteMapping("/deleteDocument/{id}")
    public void deleteDocument(@PathVariable Long id, Authentication authentication){
        service.deleteDocument(id,authentication.getName());
    }

    @PostMapping("/getPermission/{id}")
    public DocumentPermission getPermission(@PathVariable("id") Long documentId, Authentication authentication){
        return service.getPermission(documentId,authentication.getName());
    }

    @PostMapping("/sharePermission/{id}")
    public String shareDocument(
            @PathVariable Long id,
            @RequestParam String email,
            @RequestParam Role role,
            Authentication authentication) {

        service.shareDocument(
                id,
                email,
                role,
                authentication.getName()
        );

        return "Document Shared Successfully";
    }

    @GetMapping("/getVersion/{documentId}")
    public List<DocumentVersion> getVersion(@PathVariable Long documentId, Authentication authentication){

        return service.getVersion(documentId,authentication.getName());
    }

    @PutMapping("/{DocumentId}/versions/{versionId}/restore")
    public DocumentResponse restoreVersion(@PathVariable Long versionId, @PathVariable Long DocumentId, Authentication authentication){

        return service.restoreVersion(versionId,DocumentId,authentication.getName());
    }

    @GetMapping("/{id}/operations")
    public List<DocumentOperationResponse> getOperations(@PathVariable Long id) {

        return documentOperationRepository
                .findByDocumentIdOrderByVersionAsc(id)
                .stream()
                .map(operation -> new DocumentOperationResponse(
                        operation.getId(),
                        operation.getOperation(),
                        operation.getPosition(),
                        operation.getText(),
                        operation.getVersion(),
                        operation.getCreatedAt(),
                        operation.getUser().getEmail()
                ))
                .toList();
    }
}
