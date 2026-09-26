package com.example.Real_Time_Collaborative_Document_Editor.controller;

import com.example.Real_Time_Collaborative_Document_Editor.dto.DocumentUpdateRequest;
import com.example.Real_Time_Collaborative_Document_Editor.entity.Document;
import com.example.Real_Time_Collaborative_Document_Editor.entity.DocumentPermission;
import com.example.Real_Time_Collaborative_Document_Editor.entity.DocumentVersion;
import com.example.Real_Time_Collaborative_Document_Editor.enums.Role;
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


    @PostMapping("/createDocument")

    public Document createDocument(@RequestBody Document document, Authentication authentication){

        return service.createDocument(document, authentication.getName());
    }

    @GetMapping("/getAllDocuments")
    public List<Document> getAllDocument(){
        return service.getAllDocuments();
    }

    @GetMapping("/getDocument/{id}")
    public Document getDocument(@PathVariable Long id,Authentication authentication){
        return service.getDocument(id,authentication.getName());
    }

    @PutMapping("/updateDocument/{id}")
    public Document updateDocument(@PathVariable Long id, @RequestBody DocumentUpdateRequest updatedDocument, Authentication authentication){
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
    public Document restoreVersion(@PathVariable Long versionId, @PathVariable Long DocumentId, Authentication authentication){

        return service.restoreVersion(versionId,DocumentId,authentication.getName());
    }
}
