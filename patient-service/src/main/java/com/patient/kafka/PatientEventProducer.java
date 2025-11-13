package com.patient.kafka;

import java.util.concurrent.CompletableFuture;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import com.patient.event.PatientRegisteredEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PatientEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    private static final String PATIENT_REGISTERED_TOPIC = "patient.registered";

    public void publishPatientRegisteredEvent(PatientRegisteredEvent event) {
        log.info("Publishing patient registered event for patient ID: {}", event.getPatientId());
        
        CompletableFuture<SendResult<String, Object>> future = 
                kafkaTemplate.send(PATIENT_REGISTERED_TOPIC, event.getPatientId().toString(), event);
        
        future.whenComplete((result, exception) -> {
            if (exception == null) {
                log.info("Successfully published patient registered event for patient ID: {} with offset: {}", 
                        event.getPatientId(), result.getRecordMetadata().offset());
            } else {
                log.error("Failed to publish patient registered event for patient ID: {}", 
                        event.getPatientId(), exception);
            }
        });
    }
}
