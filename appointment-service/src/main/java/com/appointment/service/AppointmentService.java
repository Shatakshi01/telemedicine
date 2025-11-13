package com.appointment.service;

import com.appointment.dto.AppointmentResponse;
import com.appointment.dto.CreateAppointmentRequest;
import com.appointment.event.AppointmentBookedEvent;
import com.appointment.model.Appointment;
import com.appointment.repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final PatientEligibilityService patientEligibilityService;

    private static final String APPOINTMENT_BOOKED_TOPIC = "appointment.booked";

    public AppointmentResponse createAppointment(CreateAppointmentRequest request) {
        log.info("Creating appointment for patient {} with doctor {} on {}",
                request.getPatientId(), request.getDoctorId(), request.getAppointmentDate());

        // Check if patient is eligible for booking (within 3 days of registration)
        if (!patientEligibilityService.isPatientEligible(request.getPatientId())) {
            log.warn("Patient {} is not eligible for booking appointments", request.getPatientId());
            return null; // Simple error handling
        }

        // Create appointment
        Appointment appointment = new Appointment();
        appointment.setPatientId(request.getPatientId());
        appointment.setDoctorId(request.getDoctorId());
        appointment.setAppointmentDate(request.getAppointmentDate());
        appointment.setReason(request.getReason());
        appointment.setNotes(request.getNotes());
        appointment.setStatus(Appointment.AppointmentStatus.SCHEDULED);

        Appointment savedAppointment = appointmentRepository.save(appointment);

        // Publish appointment booked event
        publishAppointmentBookedEvent(savedAppointment);

        log.info("Appointment created successfully with ID: {}", savedAppointment.getId());
        return mapToResponse(savedAppointment);
    }

    public AppointmentResponse getAppointmentById(Long id) {
        log.info("Fetching appointment with ID: {}", id);

        Optional<Appointment> appointment = appointmentRepository.findById(id);
        if (appointment.isPresent()) {
            return mapToResponse(appointment.get());
        }

        log.warn("Appointment not found with ID: {}", id);
        return null;
    }

    public List<AppointmentResponse> getAppointmentsByPatientId(Long patientId) {
        log.info("Fetching appointments for patient: {}", patientId);

        List<Appointment> appointments = appointmentRepository.findByPatientId(patientId);
        return appointments.stream()
                .map(this::mapToResponse)
                .toList();
    }

    public AppointmentResponse updateAppointmentStatus(Long id, Appointment.AppointmentStatus status) {
        log.info("Updating appointment {} status to {}", id, status);

        Optional<Appointment> appointmentOpt = appointmentRepository.findById(id);
        if (appointmentOpt.isPresent()) {
            Appointment appointment = appointmentOpt.get();
            appointment.setStatus(status);
            Appointment updatedAppointment = appointmentRepository.save(appointment);
            return mapToResponse(updatedAppointment);
        }

        log.warn("Appointment not found with ID: {}", id);
        return null;
    }

    private void publishAppointmentBookedEvent(Appointment appointment) {
        try {
            AppointmentBookedEvent event = new AppointmentBookedEvent();
            event.setAppointmentId(appointment.getId());
            event.setPatientId(appointment.getPatientId());
            event.setDoctorId(appointment.getDoctorId());
            event.setAppointmentDate(appointment.getAppointmentDate());
            event.setStatus(appointment.getStatus().toString());
            event.setReason(appointment.getReason());
            event.setBookedAt(LocalDateTime.now());

            kafkaTemplate.send(APPOINTMENT_BOOKED_TOPIC, event.getAppointmentId().toString(), event);
            log.info("Published appointment booked event for appointment: {}", appointment.getId());
        } catch (Exception e) {
            log.error("Failed to publish appointment booked event: {}", e.getMessage());
        }
    }

    private AppointmentResponse mapToResponse(Appointment appointment) {
        AppointmentResponse response = new AppointmentResponse();
        response.setId(appointment.getId());
        response.setPatientId(appointment.getPatientId());
        response.setDoctorId(appointment.getDoctorId());
        response.setAppointmentDate(appointment.getAppointmentDate());
        response.setStatus(appointment.getStatus());
        response.setReason(appointment.getReason());
        response.setNotes(appointment.getNotes());
        response.setCreatedAt(appointment.getCreatedAt());
        response.setUpdatedAt(appointment.getUpdatedAt());
        return response;
    }
}
