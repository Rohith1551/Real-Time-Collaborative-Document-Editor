package com.example.Real_Time_Collaborative_Document_Editor.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class DocumentOperationResponse {

    private Long id;
    private String operation;
    private Integer position;
    private String text;
    private Long version;
    private LocalDateTime createdAt;
    private String email;
}