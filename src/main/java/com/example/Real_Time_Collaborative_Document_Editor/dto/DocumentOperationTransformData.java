package com.example.Real_Time_Collaborative_Document_Editor.dto;

public record DocumentOperationTransformData(
        String operation,
        Integer position,
        String text,
        Long version
) {}