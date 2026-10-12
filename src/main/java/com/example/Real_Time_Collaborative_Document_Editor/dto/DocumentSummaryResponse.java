package com.example.Real_Time_Collaborative_Document_Editor.dto;

public record DocumentSummaryResponse(
        Long id,
        String title,
        String content,
        Long version
) {
}