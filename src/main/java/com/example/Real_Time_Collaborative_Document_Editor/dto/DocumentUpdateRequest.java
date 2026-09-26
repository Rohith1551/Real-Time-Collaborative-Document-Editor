package com.example.Real_Time_Collaborative_Document_Editor.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DocumentUpdateRequest {

    private Long version;
    private String title;
    private String content;
}
