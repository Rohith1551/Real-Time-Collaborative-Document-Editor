package com.example.Real_Time_Collaborative_Document_Editor.controller;

import com.example.Real_Time_Collaborative_Document_Editor.dto.DocumentWebSocketMessage;
import com.example.Real_Time_Collaborative_Document_Editor.entity.Document;
import com.example.Real_Time_Collaborative_Document_Editor.service.DocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
public class DocumentWebSocketController {

    @Autowired
    private DocumentService documentService;
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/document")
    public void handleDocumentMessage(DocumentWebSocketMessage message, Principal principal)
    {
        String email = principal.getName();


        Document document = documentService.applyOperation(
                message.getDocumentId(),
                message.getOperation(),
                message.getPosition(),
                message.getText(),
                message.getVersion(),
                email
        );

        message.setContent(document.getContent());
        message.setVersion(document.getVersion());

        String destination = "/topic/document/" + message.getDocumentId();

        messagingTemplate.convertAndSend(destination,message);
    }
}
