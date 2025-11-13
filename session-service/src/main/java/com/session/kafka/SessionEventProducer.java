package com.session.kafka;

import com.session.event.SessionStartedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
@Slf4j
public class SessionEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    private static final String SESSION_STARTED_TOPIC = "session.started";

    public void publishSessionStartedEvent(SessionStartedEvent event) {
        try {
            // Set base event properties
            event.setEventId(UUID.randomUUID().toString());
            event.setEventType("SESSION_STARTED");
            event.setTimestamp(LocalDateTime.now());
            event.setSource("session-service");
            
            log.info("Publishing session.started event for session ID: {}", event.getSessionId());
            
            CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(SESSION_STARTED_TOPIC, event.getSessionId(), event);
            
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("Successfully published session.started event for session ID: {} to topic: {} with offset: {}", 
                            event.getSessionId(), SESSION_STARTED_TOPIC, result.getRecordMetadata().offset());
                } else {
                    log.error("Failed to publish session.started event for session ID: {} to topic: {}", 
                            event.getSessionId(), SESSION_STARTED_TOPIC, ex);
                }
            });
            
        } catch (Exception e) {
            log.error("Error publishing session.started event for session ID: {}", event.getSessionId(), e);
            throw new RuntimeException("Failed to publish session started event", e);
        }
    }
}
