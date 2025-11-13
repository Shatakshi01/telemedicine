package com.session.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.session.event.AppointmentBookedEvent;
import com.session.service.AppointmentMappingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AppointmentEventListener {

    private final AppointmentMappingService appointmentMappingService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "appointment.booked", groupId = "session-service-group", containerFactory = "kafkaListenerContainerFactory")
    public void handleAppointmentBooked(@Payload String message, Acknowledgment ack) {
        try {
            log.info("Received appointment.booked event from topic: appointment.booked");
            log.debug("Raw event payload: {}", message);

            // Parse the JSON message manually
            AppointmentBookedEvent event = objectMapper.readValue(message, AppointmentBookedEvent.class);

            log.debug("Parsed event: appointmentId={}, patientId={}, doctorId={}, appointmentDate={}",
                    event.getAppointmentId(), event.getPatientId(), event.getDoctorId(), event.getAppointmentDate());

            processAppointmentEvent(event);

            // Acknowledge successful processing
            ack.acknowledge();

        } catch (Exception e) {
            log.error("Error processing appointment.booked event: {}", e.getMessage(), e);
            log.error("Raw message that failed: {}", message);

            // In production, you might want to:
            // 1. Send this to a dead letter queue
            // 2. Implement retry logic with exponential backoff
            // 3. Alert monitoring systems
            //
            // For now, we'll acknowledge to prevent infinite retries
            // In a real system, you'd want more sophisticated error handling
            ack.acknowledge();
        }
    }

    private void processAppointmentEvent(AppointmentBookedEvent event) {
        log.info("Processing appointment.booked event for appointment ID: {}", event.getAppointmentId());

        // Step 1: Save appointment mapping first (separate schema)
        // Use appointmentDate as it contains the actual appointment date/time
        appointmentMappingService.saveAppointmentMapping(
                event.getAppointmentId(),
                event.getPatientId(),
                event.getDoctorId(),
                event.getAppointmentType(),
                event.getAppointmentDate());

        log.info("Successfully saved appointment mapping for appointment ID: {}", event.getAppointmentId());

        // Step 2: Mark mapping as confirmed to allow session creation later
        appointmentMappingService.updateMappingStatus(
                event.getAppointmentId(),
                com.session.entity.AppointmentMapping.AppointmentStatus.CONFIRMED);

        log.info("Appointment mapping confirmed for appointment ID: {}", event.getAppointmentId());
    }
}
