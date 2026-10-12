package com.example.Real_Time_Collaborative_Document_Editor.kafka;

public class DocumentUpdatedEvent {

    private Long documentId;
    private Long userId;
    private String operation;
    private Long version;

    public DocumentUpdatedEvent() {
    }

    public DocumentUpdatedEvent(
            Long documentId,
            Long userId,
            String operation,
            Long version
    ) {
        this.documentId = documentId;
        this.userId = userId;
        this.operation = operation;
        this.version = version;
    }

    public Long getDocumentId() {
        return documentId;
    }

    public Long getUserId() {
        return userId;
    }

    public String getOperation() {
        return operation;
    }

    public Long getVersion() {
        return version;
    }
}