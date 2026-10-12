package com.example.Real_Time_Collaborative_Document_Editor.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic documentUpdatedTopic() {
        return new NewTopic(
                "document-updated",
                1,
                (short) 1
        );
    }
}