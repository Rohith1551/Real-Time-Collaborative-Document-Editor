
package com.example.Real_Time_Collaborative_Document_Editor.service;

import com.example.Real_Time_Collaborative_Document_Editor.dto.DocumentOperationTransformData;
import com.example.Real_Time_Collaborative_Document_Editor.dto.DocumentResponse;
import com.example.Real_Time_Collaborative_Document_Editor.dto.DocumentSummaryResponse;
import com.example.Real_Time_Collaborative_Document_Editor.dto.DocumentUpdateRequest;
import com.example.Real_Time_Collaborative_Document_Editor.entity.*;
import com.example.Real_Time_Collaborative_Document_Editor.enums.Role;
import com.example.Real_Time_Collaborative_Document_Editor.exceptions.DocumentVersionConflictException;
import com.example.Real_Time_Collaborative_Document_Editor.kafka.DocumentEventProducer;
import com.example.Real_Time_Collaborative_Document_Editor.kafka.DocumentUpdatedEvent;
import com.example.Real_Time_Collaborative_Document_Editor.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DocumentService {

    private final ConcurrentHashMap<Long, Object> documentLocks =
            new ConcurrentHashMap<>();

    private final DocumentEventProducer documentEventProducer;
    private final DocumentRepository documentrepo;
    private final UserRepository userRepository;
    private final DocumentPermissionRepository documentpermissionRepo;
    private final DocumentVersionRepository documentVersionRepository;
    private final DocumentOperationRepository documentOperationRepository;

    public DocumentService(
            DocumentEventProducer documentEventProducer,
            DocumentRepository documentrepo,
            UserRepository userRepository,
            DocumentPermissionRepository documentpermissionRepo,
            DocumentVersionRepository documentVersionRepository,
            DocumentOperationRepository documentOperationRepository) {

        this.documentEventProducer = documentEventProducer;
        this.documentrepo = documentrepo;
        this.userRepository = userRepository;
        this.documentpermissionRepo = documentpermissionRepo;
        this.documentVersionRepository = documentVersionRepository;
        this.documentOperationRepository = documentOperationRepository;
    }

    public Document createDocument(Document document, String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        document.setOwner(user);

        Document savedDocument = documentrepo.save(document);

        DocumentPermission permission = new DocumentPermission();
        permission.setDocument(savedDocument);
        permission.setUser(user);
        permission.setRole(Role.OWNER);

        documentpermissionRepo.save(permission);

        return savedDocument;
    }

    public DocumentResponse getDocument(Long id, String email) {

        getPermission(id, email);

        return documentrepo.findDocumentResponseById(id)
                .orElseThrow(() -> new RuntimeException("Document not found"));
    }

    public List<DocumentSummaryResponse> getAllDocuments() {
        return documentrepo.findAllDocumentSummaries();
    }

    @Transactional
    public DocumentResponse updateDocument(
            Long id,
            DocumentUpdateRequest updatedDoc,
            String email) {

        DocumentPermission permission = getPermission(id, email);

        if (permission.getRole() != Role.OWNER
                && permission.getRole() != Role.EDITOR) {
            throw new RuntimeException(
                    "You do not have permission to edit this document");
        }

        Document document = documentrepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Document Not Found"));

        if (document.getVersion() != 0
                && !document.getVersion().equals(updatedDoc.getVersion())) {
            throw new DocumentVersionConflictException(
                    "Document was modified by another user", id);
        }

        DocumentVersion version = new DocumentVersion();
        version.setDocument(document);
        version.setContent(document.getContent());
        version.setCreatedAt(LocalDateTime.now());
        version.setTitle(document.getTitle());

        documentVersionRepository.save(version);

        document.setContent(updatedDoc.getContent());
        document.setTitle(updatedDoc.getTitle());

        documentrepo.save(document);

        return new DocumentResponse(
                document.getId(),
                document.getTitle(),
                document.getContent(),
                document.getVersion());
    }

    public void deleteDocument(Long id, String email) {

        Document document = documentrepo.findByIdAndOwnerEmail(id, email)
                .orElseThrow(() -> new RuntimeException(
                        "Only the owner can delete the document"));

        documentrepo.delete(document);
    }

    public void shareDocument(
            Long documentId,
            String email,
            Role role,
            String ownerEmail) {

        Document document = documentrepo.findByIdAndOwnerEmail(
                        documentId, ownerEmail)
                .orElseThrow(() -> new RuntimeException("Document Not Found"));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User Not Found"));

        DocumentPermission permission = new DocumentPermission();
        permission.setDocument(document);
        permission.setUser(user);
        permission.setRole(role);

        documentpermissionRepo.save(permission);
    }

    public DocumentPermission getPermission(Long documentId, String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return getPermission(documentId, user);
    }

    public DocumentPermission getPermission(Long documentId, User user) {

        return documentpermissionRepo.findByDocumentIdAndUserId(
                        documentId, user.getId())
                .orElseThrow(() -> new RuntimeException(
                        "No Permission for this Document"));
    }

    public List<DocumentVersion> getVersion(Long documentId, String email) {

        getPermission(documentId, email);

        return documentVersionRepository
                .findByDocumentIdOrderByCreatedAtDesc(documentId);
    }

    @Transactional
    public DocumentResponse restoreVersion(
            Long id,
            Long documentId,
            String email) {

        Document document = documentrepo.findByIdAndOwnerEmail(
                        documentId, email)
                .orElseThrow(() -> new RuntimeException("Document Not Found"));

        DocumentVersion version = documentVersionRepository
                .findByIdAndDocumentId(id, documentId)
                .orElseThrow(() -> new RuntimeException(
                        "Document Version Not Found"));

        if (document.getTitle().equals(version.getTitle())
                && document.getContent().equals(version.getContent())) {
            throw new RuntimeException("Document is already at this version");
        }

        boolean versionExists = documentVersionRepository
                .existsByDocumentIdAndTitleAndContent(
                        documentId,
                        document.getTitle(),
                        document.getContent());

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

        documentrepo.save(document);

        return new DocumentResponse(
                document.getId(),
                document.getTitle(),
                document.getContent(),
                document.getVersion());
    }

    @Transactional
    public Document applyOperation(
            Long documentId,
            String operation,
            Integer position,
            String text,
            Long version,
            String email) {

        Object lock = documentLocks.computeIfAbsent(
                documentId, id -> new Object());

        synchronized (lock) {

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            DocumentPermission permission = getPermission(documentId, user);

            if (permission.getRole() != Role.OWNER
                    && permission.getRole() != Role.EDITOR) {
                throw new RuntimeException(
                        "You do not have permission to edit this document");
            }

            Document document = documentrepo.findById(documentId)
                    .orElseThrow(() -> new RuntimeException("Document Not Found"));

            if (version == null) {
                throw new DocumentVersionConflictException(
                        "Document version cannot be null", documentId);
            }

            if (version > document.getVersion()) {
                throw new DocumentVersionConflictException(
                        "Invalid document version", documentId);
            }

            if (position == null || position < 0) {
                throw new RuntimeException("Invalid position");
            }

            if (!"INSERT".equals(operation) && !"DELETE".equals(operation)) {
                throw new RuntimeException("Invalid Operation");
            }

            List<DeleteRange> deleteRanges = new ArrayList<>();

            if ("DELETE".equals(operation)) {
                if (text == null) {
                    throw new RuntimeException("Text cannot be null");
                }

                if (!text.isEmpty()) {
                    deleteRanges.add(new DeleteRange(position, text));
                }
            }

            /*
             * Operational Transformation:
             * transform the incoming operation against each operation
             * committed since the client's document version.
             */
            if (!document.getVersion().equals(version)) {

                List<DocumentOperationTransformData> previousOperations =
                        documentOperationRepository.findOperationsForTransformation(
                                documentId, version);

                for (DocumentOperationTransformData previousOperation
                        : previousOperations) {

                    String previousType = previousOperation.operation();

                    /*
                     * DELETE vs DELETE
                     */
                    if ("DELETE".equals(operation)
                            && "DELETE".equals(previousType)) {

                        List<DeleteRange> transformedRanges = new ArrayList<>();

                        for (DeleteRange range : deleteRanges) {

                            DeleteTransformation result = transformDeleteOperation(
                                    range.getPosition(),
                                    range.getText(),
                                    previousOperation.position(),
                                    previousOperation.text());

                            transformedRanges.addAll(result.getRanges());
                        }

                        deleteRanges = transformedRanges;

                        if (deleteRanges.isEmpty()) {
                            return document;
                        }
                    }

                    /*
                     * DELETE vs INSERT:
                     * split a deletion when another user's insertion lies
                     * inside its original text range, preserving inserted text.
                     */
                    else if ("DELETE".equals(operation)
                            && "INSERT".equals(previousType)) {

                        List<DeleteRange> transformedRanges = new ArrayList<>();

                        int insertPosition = previousOperation.position();
                        String insertedText = previousOperation.text() == null
                                ? ""
                                : previousOperation.text();

                        int insertedLength = insertedText.length();

                        for (DeleteRange range : deleteRanges) {

                            int start = range.getPosition();
                            String deleteText = range.getText();

                            if (deleteText == null || deleteText.isEmpty()) {
                                continue;
                            }

                            int end = start + deleteText.length();

                            if (insertPosition <= start) {
                                // Insertion is at or before the deletion.
                                transformedRanges.add(new DeleteRange(
                                        start + insertedLength,
                                        deleteText));
                            } else if (insertPosition >= end) {
                                // Insertion is after the deletion.
                                transformedRanges.add(range);
                            } else {
                                // Insertion is inside the deletion: split it.
                                int splitIndex = insertPosition - start;

                                String prefix = deleteText.substring(0, splitIndex);
                                String suffix = deleteText.substring(splitIndex);

                                if (!prefix.isEmpty()) {
                                    transformedRanges.add(new DeleteRange(
                                            start, prefix));
                                }

                                if (!suffix.isEmpty()) {
                                    transformedRanges.add(new DeleteRange(
                                            insertPosition + insertedLength,
                                            suffix));
                                }
                            }
                        }

                        deleteRanges = transformedRanges;
                    }

                    /*
                     * INSERT vs INSERT, or INSERT vs DELETE
                     */
                    else if ("INSERT".equals(operation)) {

                        position = transformOperationPosition(
                                operation,
                                position,
                                text,
                                previousOperation);
                    }

                    /*
                     * Fallback for other operation combinations.
                     */
                    else if ("DELETE".equals(operation)) {

                        List<DeleteRange> transformedRanges = new ArrayList<>();

                        for (DeleteRange range : deleteRanges) {

                            Integer transformedPosition =
                                    transformOperationPosition(
                                            operation,
                                            range.getPosition(),
                                            range.getText(),
                                            previousOperation);

                            transformedRanges.add(new DeleteRange(
                                    transformedPosition,
                                    range.getText()));
                        }

                        deleteRanges = transformedRanges;
                    }
                }
            }

            String content = document.getContent();

            if (content == null) {
                content = "";
            }

            /*
             * Apply INSERT
             */
            if ("INSERT".equals(operation)) {

                if (text == null) {
                    throw new RuntimeException("Text cannot be null");
                }

                if (position > content.length()) {
                    throw new RuntimeException("Invalid position");
                }

                content = content.substring(0, position)
                        + text
                        + content.substring(position);
            }

            /*
             * Apply DELETE ranges from highest position to lowest.
             */
            else {

                if (deleteRanges.isEmpty()) {
                    return document;
                }

                deleteRanges.sort(
                        Comparator.comparing(DeleteRange::getPosition)
                                .reversed());

                for (DeleteRange range : deleteRanges) {

                    Integer deletePosition = range.getPosition();
                    String deleteText = range.getText();

                    if (deleteText == null || deleteText.isEmpty()) {
                        continue;
                    }

                    if (deletePosition == null
                            || deletePosition < 0
                            || deletePosition > content.length()) {
                        throw new RuntimeException("Invalid position");
                    }

                    int deleteEnd = deletePosition + deleteText.length();

                    if (deleteEnd > content.length()) {
                        throw new RuntimeException("Invalid delete range");
                    }

                    String target = content.substring(deletePosition, deleteEnd);

                    if (!target.equals(deleteText)) {
                        throw new RuntimeException(
                                "Delete text does not match document content");
                    }

                    content = content.substring(0, deletePosition)
                            + content.substring(deleteEnd);
                }
            }

            /*
             * Save the previous document state.
             */
            DocumentVersion documentVersion = new DocumentVersion();
            documentVersion.setDocument(document);
            documentVersion.setTitle(document.getTitle());
            documentVersion.setCreatedAt(LocalDateTime.now());
            documentVersion.setContent(document.getContent());

            documentVersionRepository.save(documentVersion);

            /*
             * Persist the updated document.
             */
            document.setContent(content);

            Document savedDocument = documentrepo.saveAndFlush(document);

            /*
             * Persist operation history.
             */
            if ("DELETE".equals(operation)) {

                List<DeleteRange> historyRanges = new ArrayList<>(deleteRanges);

                historyRanges.sort(
                        Comparator.comparing(DeleteRange::getPosition));

                for (DeleteRange range : historyRanges) {

                    if (range.getText() == null || range.getText().isEmpty()) {
                        continue;
                    }

                    DocumentOperation history = new DocumentOperation();
                    history.setDocument(savedDocument);
                    history.setUser(user);
                    history.setOperation("DELETE");
                    history.setPosition(range.getPosition());
                    history.setText(range.getText());
                    history.setVersion(savedDocument.getVersion());
                    history.setCreatedAt(LocalDateTime.now());

                    documentOperationRepository.save(history);
                }
            } else {

                DocumentOperation history = new DocumentOperation();
                history.setDocument(savedDocument);
                history.setUser(user);
                history.setOperation(operation);
                history.setPosition(position);
                history.setText(text);
                history.setVersion(savedDocument.getVersion());
                history.setCreatedAt(LocalDateTime.now());

                documentOperationRepository.save(history);
            }

            /*
             * Publish the event after saving the document and operation history.
             */
            DocumentUpdatedEvent event = new DocumentUpdatedEvent(
                    savedDocument.getId(),
                    user.getId(),
                    operation,
                    savedDocument.getVersion());

            documentEventProducer.publishDocumentUpdated(event);

            return savedDocument;
        }
    }

    private static class DeleteRange {

        private final Integer position;
        private final String text;

        public DeleteRange(Integer position, String text) {
            this.position = position;
            this.text = text;
        }

        public Integer getPosition() {
            return position;
        }

        public String getText() {
            return text;
        }
    }

    private static class DeleteTransformation {

        private final List<DeleteRange> ranges;

        public DeleteTransformation(List<DeleteRange> ranges) {
            this.ranges = ranges;
        }

        public List<DeleteRange> getRanges() {
            return ranges;
        }
    }

    /*
     * INSERT vs INSERT
     */
    private Integer transformInsertPosition(
            Integer incomingPosition,
            Integer previousPosition,
            String previousText) {

        if (previousPosition <= incomingPosition) {
            return incomingPosition + previousText.length();
        }

        return incomingPosition;
    }

    /*
     * DELETE vs DELETE
     */
    private DeleteTransformation transformDeleteOperation(
            Integer incomingPosition,
            String incomingText,
            Integer previousPosition,
            String previousText) {

        List<DeleteRange> result = new ArrayList<>();

        if (incomingText == null) {
            incomingText = "";
        }

        if (previousText == null) {
            previousText = "";
        }

        if (incomingText.isEmpty()) {
            return new DeleteTransformation(result);
        }

        int incomingStart = incomingPosition;
        int incomingEnd = incomingPosition + incomingText.length();

        int previousStart = previousPosition;
        int previousEnd = previousPosition + previousText.length();

        // Previous deletion is completely before incoming deletion.
        if (previousEnd <= incomingStart) {
            result.add(new DeleteRange(
                    incomingPosition - previousText.length(),
                    incomingText));

            return new DeleteTransformation(result);
        }

        // Previous deletion is completely after incoming deletion.
        if (previousStart >= incomingEnd) {
            result.add(new DeleteRange(incomingPosition, incomingText));

            return new DeleteTransformation(result);
        }

        // Previous deletion completely covers incoming deletion.
        if (previousStart <= incomingStart && previousEnd >= incomingEnd) {
            return new DeleteTransformation(result);
        }

        // Previous deletion is completely inside incoming deletion.
        if (previousStart > incomingStart && previousEnd < incomingEnd) {

            int prefixLength = previousStart - incomingStart;
            int suffixStart = previousEnd - incomingStart;

            String prefix = incomingText.substring(0, prefixLength);
            String suffix = incomingText.substring(suffixStart);

            if (!prefix.isEmpty()) {
                result.add(new DeleteRange(incomingStart, prefix));
            }

            if (!suffix.isEmpty()) {
                result.add(new DeleteRange(previousStart, suffix));
            }

            return new DeleteTransformation(result);
        }

        // Previous deletion overlaps the beginning of incoming deletion.
        if (previousStart <= incomingStart && previousEnd < incomingEnd) {

            int overlapLength = previousEnd - incomingStart;
            String remainingText = incomingText.substring(overlapLength);

            if (!remainingText.isEmpty()) {
                result.add(new DeleteRange(previousStart, remainingText));
            }

            return new DeleteTransformation(result);
        }

        // Previous deletion overlaps the end of incoming deletion.
        if (previousStart > incomingStart && previousEnd >= incomingEnd) {

            int remainingLength = previousStart - incomingStart;
            String remainingText = incomingText.substring(0, remainingLength);

            if (!remainingText.isEmpty()) {
                result.add(new DeleteRange(incomingStart, remainingText));
            }

            return new DeleteTransformation(result);
        }

        return new DeleteTransformation(result);
    }

    /*
     * INSERT vs INSERT
     * INSERT vs DELETE
     * DELETE vs INSERT (single-position transformation)
     */
    private Integer transformOperationPosition(
            String incomingOperation,
            Integer incomingPosition,
            String incomingText,
            DocumentOperationTransformData previousOperation) {

        String previousOperationType = previousOperation.operation();
        Integer previousPosition = previousOperation.position();

        String previousText = previousOperation.text() == null
                ? ""
                : previousOperation.text();

        if (incomingText == null) {
            incomingText = "";
        }

        // Incoming INSERT
        if ("INSERT".equals(incomingOperation)) {

            // INSERT vs INSERT
            if ("INSERT".equals(previousOperationType)) {
                return transformInsertPosition(
                        incomingPosition,
                        previousPosition,
                        previousText);
            }

            // INSERT vs DELETE
            if ("DELETE".equals(previousOperationType)) {

                int deletedLength = previousText.length();

                if (previousPosition < incomingPosition) {
                    return Math.max(
                            previousPosition,
                            incomingPosition - deletedLength);
                }

                return incomingPosition;
            }
        }

        // Incoming DELETE
        if ("DELETE".equals(incomingOperation)) {

            // DELETE vs INSERT; used for a single contiguous range.
            if ("INSERT".equals(previousOperationType)) {

                int insertedLength = previousText.length();

                if (previousPosition <= incomingPosition) {
                    return incomingPosition + insertedLength;
                }

                return incomingPosition;
            }
        }

        return incomingPosition;
    }
}
