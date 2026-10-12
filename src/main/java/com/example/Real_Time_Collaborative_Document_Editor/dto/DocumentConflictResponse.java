package com.example.Real_Time_Collaborative_Document_Editor.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DocumentConflictResponse {

    private String message;
    private Long currentVersion;
    private String currentContent;
}