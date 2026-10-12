package com.example.Real_Time_Collaborative_Document_Editor.kafka;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class DocumentEventProducer {

    private final KafkaTemplate<String, DocumentUpdatedEvent> kafkaTemplate;

    public DocumentEventProducer(
            KafkaTemplate<String, DocumentUpdatedEvent> kafkaTemplate
    ) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishDocumentUpdated(DocumentUpdatedEvent event) {

        kafkaTemplate.send(
                "document-updated",
                String.valueOf(event.getDocumentId()),
                event
        );
    }
}