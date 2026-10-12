package com.example.Real_Time_Collaborative_Document_Editor.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class DocumentEventConsumer {

    @KafkaListener(topics = "document-updated")
    public void consume(DocumentUpdatedEvent event) {

        System.out.println(
                "KAFKA EVENT RECEIVED: " +
                        "documentId=" + event.getDocumentId() +
                        ", userId=" + event.getUserId() +
                        ", operation=" + event.getOperation() +
                        ", version=" + event.getVersion()
        );
    }
}