package com.appointment.listener;

import com.appointment.event.PatientRegisteredEvent;
import com.appointment.service.PatientEligibilityService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PatientEventListener {

    private final PatientEligibilityService patientEligibilityService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "patient.registered", groupId = "appointment-service")
    public void handlePatientRegistered(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {

        try {
            log.info("Received patient.registered event from topic: {}, partition: {}, offset: {}",
                    topic, partition, offset);
            log.debug("Raw event payload: {}", message);

            // Manual deserialization with error handling
            PatientRegisteredEvent event = objectMapper.readValue(message, PatientRegisteredEvent.class);

            log.info("Successfully deserialized event for patient ID: {}", event.getPatientId());

            // Add patient to database for eligibility tracking
            patientEligibilityService.addPatient(event.getPatientId(), event.getPhoneNumber());

            log.info("Patient {} is now eligible for booking appointments for 3 days", event.getPatientId());

            // Acknowledge the message
            acknowledgment.acknowledge();

        } catch (Exception e) {
            log.error("Error processing patient.registered event: {}", e.getMessage(), e);
            log.error("Raw message that failed: {}", message);
            // In a production environment, you might want to send this to a dead letter
            // queue
            // For now, we'll still acknowledge to prevent infinite retries
            acknowledgment.acknowledge();
        }
    }
}
