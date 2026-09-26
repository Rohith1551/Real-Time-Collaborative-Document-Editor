package com.example.Real_Time_Collaborative_Document_Editor.service;

import com.example.Real_Time_Collaborative_Document_Editor.dto.DocumentUpdateRequest;
import com.example.Real_Time_Collaborative_Document_Editor.entity.Document;
import com.example.Real_Time_Collaborative_Document_Editor.entity.DocumentPermission;
import com.example.Real_Time_Collaborative_Document_Editor.entity.DocumentVersion;
import com.example.Real_Time_Collaborative_Document_Editor.entity.User;
import com.example.Real_Time_Collaborative_Document_Editor.enums.Role;
import com.example.Real_Time_Collaborative_Document_Editor.repository.DocumentPermissionRepository;
import com.example.Real_Time_Collaborative_Document_Editor.repository.DocumentRepository;
import com.example.Real_Time_Collaborative_Document_Editor.repository.DocumentVersionRepository;
import com.example.Real_Time_Collaborative_Document_Editor.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DocumentService {

    @Autowired
    private DocumentRepository documentrepo;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private DocumentPermissionRepository documentpermissionRepo;
    @Autowired
    private DocumentVersionRepository documentVersionRepository;



    public Document createDocument(Document document,String email){

        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        document.setOwner(user);
        Document savedDocument = documentrepo.save(document);

        DocumentPermission permission = new DocumentPermission();
        permission.setDocument(savedDocument);
        permission.setUser(user);
        permission.setRole(Role.OWNER);

        documentpermissionRepo.save(permission);
        return savedDocument;
    }

    public Document getDocument(Long id, String email) {

        getPermission(id, email);

        return documentrepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Document not found"));
    }

    public List<Document> getAllDocuments(){

        return documentrepo.findAll();

    }


    @Transactional
    public Document updateDocument(Long id, DocumentUpdateRequest updatedDoc, String email) {

        DocumentPermission permission = getPermission(id, email);

        if (permission.getRole() != Role.OWNER &&
                permission.getRole() != Role.EDITOR) {
            throw new RuntimeException("You do not have permission to edit this document");
        }

        Document document = documentrepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Document Not Found"));

        if(document.getVersion() != 0 && !document.getVersion().equals(updatedDoc.getVersion())){
            throw new RuntimeException("Document was modified by another user");
        }

        DocumentVersion version = new DocumentVersion();
        version.setDocument(document);
        version.setContent(document.getContent());
        version.setCreatedAt(LocalDateTime.now());
        version.setTitle(document.getTitle());

        documentVersionRepository.save(version);



        document.setContent(updatedDoc.getContent());
        document.setTitle(updatedDoc.getTitle());


        return documentrepo.save(document);
    }
    public void deleteDocument(Long id, String email) {

        Document document = documentrepo.findByIdAndOwnerEmail(id,email).orElseThrow(() -> new RuntimeException("Only the owner can delete the document"));

        documentrepo.delete(document);
    }

    public DocumentPermission getPermission(Long DocumentId, String email){

        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

        return documentpermissionRepo.findByDocumentIdAndUserId(DocumentId,user.getId()).orElseThrow(() -> new RuntimeException("No Permission for this Document"));

    }

    public void shareDocument(Long DocumentId, String email, Role role, String ownerEmail){

        Document document = documentrepo.findByIdAndOwnerEmail(DocumentId,ownerEmail).orElseThrow(() -> new RuntimeException("Document Not Found"));
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Document Not Found"));

        DocumentPermission permission = new DocumentPermission();
        permission.setDocument(document);
        permission.setUser(user);
        permission.setRole(role);
        documentpermissionRepo.save(permission);
    }

    public List<DocumentVersion> getVersion(Long documentId, String email){

        getPermission(documentId,email);

        return documentVersionRepository.findByDocumentIdOrderByCreatedAtDesc(documentId);
    }

    @Transactional
    public Document restoreVersion(Long id, Long DocumentId, String email) {

        Document document = documentrepo.findByIdAndOwnerEmail(DocumentId, email)
                .orElseThrow(() -> new RuntimeException("Document Not Found"));

        DocumentVersion version = documentVersionRepository.findByIdAndDocumentId(id, DocumentId)
                .orElseThrow(() -> new RuntimeException("Document Version Not Found"));

        if (document.getTitle().equals(version.getTitle()) &&
                document.getContent().equals(version.getContent())) {
            throw new RuntimeException("Document is already at this version");
        }

        boolean versionExists = documentVersionRepository
                .existsByDocumentIdAndTitleAndContent(
                        DocumentId,
                        document.getTitle(),
                        document.getContent()
                );

        if (!versionExists) {
            DocumentVersion currentVersion = new DocumentVersion();
            currentVersion.setDocument(document);
            currentVersion.setContent(document.getContent());
            currentVersion.setCreatedAt(LocalDateTime.now());
            currentVersion.setTitle(document.getTitle());

            documentVersionRepository.save(currentVersion);
        }

      

        document.setTitle(version.getTitle());
        document.setContent(version.getContent());

        return documentrepo.save(document);
    }

    public Document applyOperation(Long documentId,String operation,Integer position,String text,Long version,String email){

        DocumentPermission permission = getPermission(documentId,email);
        if(permission.getRole() != Role.OWNER && permission.getRole() != Role.EDITOR){
            throw new RuntimeException("You do not have permission to edit this document");
        }

        Document document = documentrepo.findById(documentId).orElseThrow(() -> new RuntimeException("Document Not Found"));

        System.out.println("Incoming version: "+version);
        System.out.println("Database version: "+document.getVersion());
        if(!document.getVersion().equals(version)){
            throw new RuntimeException("Document was modified by another user");
        }
        String content = document.getContent();

        if(operation.equals("INSERT")) {

            content = content.substring(0, position)
                    + text
                    + content.substring(position);
        } else if(operation.equals("DELETE")){

            content = content.substring(0,position) + content.substring(position+text.length());
        } else {
            throw new RuntimeException("Invalid Operation");
        }
        document.setContent(content);

        return documentrepo.save(document);
    }




}
