package com.example.Real_Time_Collaborative_Document_Editor.exceptions;

import lombok.Getter;

@Getter
public class DocumentVersionConflictException extends RuntimeException {

    private final Long documentId;

    public DocumentVersionConflictException(String message, Long documentId) {
        super(message);
        this.documentId = documentId;
    }
}