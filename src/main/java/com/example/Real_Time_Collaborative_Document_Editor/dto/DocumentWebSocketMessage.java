package com.example.Real_Time_Collaborative_Document_Editor.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.security.config.annotation.web.saml2.Saml2SecurityMarker;

@Getter
@Setter
public class DocumentWebSocketMessage {

    private Long documentId;
    private String operation;
    private Integer position;
    private String text;
    private String content;

    private Long version;

}
