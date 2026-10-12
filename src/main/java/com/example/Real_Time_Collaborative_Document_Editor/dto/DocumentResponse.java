package com.example.Real_Time_Collaborative_Document_Editor.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DocumentResponse {

    private Long id;
    private String title;
    private String content;
    private Long version;
}