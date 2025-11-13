package com.appointment.controller;

import com.appointment.dto.AppointmentResponse;
import com.appointment.dto.CreateAppointmentRequest;
import com.appointment.model.Appointment;
import com.appointment.model.Patient;
import com.appointment.service.AppointmentService;
import com.appointment.service.PatientEligibilityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/appointments")
@Slf4j
@Tag(name = "Appointment Management", description = "Simple APIs for managing patient appointments")
public class AppointmentController {


        private final AppointmentService appointmentService;
        private final PatientEligibilityService patientEligibilityService;

        

        public AppointmentController(AppointmentService appointmentService,
                        PatientEligibilityService patientEligibilityService) {
                this.appointmentService = appointmentService;
                this.patientEligibilityService = patientEligibilityService;
        }

        @PostMapping
        @Operation(summary = "Book a new appointment", description = "Creates a new appointment for an eligible patient")
        public ResponseEntity<AppointmentResponse> bookAppointment(
                        @Valid @RequestBody CreateAppointmentRequest request) {
                log.info("Received request to book appointment: {}", request);

                AppointmentResponse response = appointmentService.createAppointment(request);

                if (response != null) {
                        return ResponseEntity.ok(response);
                } else {
                        return ResponseEntity.badRequest().build();
                }
        }

        @GetMapping("/{id}")
        @Operation(summary = "Get appointment details", description = "Retrieves appointment details by ID")
        public ResponseEntity<AppointmentResponse> getAppointment(@PathVariable Long id) {
                log.info("Received request to get appointment with ID: {}", id);

                AppointmentResponse response = appointmentService.getAppointmentById(id);

                if (response != null) {
                        return ResponseEntity.ok(response);
                } else {
                        return ResponseEntity.notFound().build();
                }
        }

        @GetMapping("/patient/{patientId}")
        @Operation(summary = "Get appointments by patient ID", description = "Retrieves all appointments for a patient")
        public ResponseEntity<List<AppointmentResponse>> getAppointmentsByPatient(@PathVariable Long patientId) {
                log.info("Received request to get appointments for patient: {}", patientId);

                List<AppointmentResponse> responses = appointmentService.getAppointmentsByPatientId(patientId);
                return ResponseEntity.ok(responses);
        }

        @PutMapping("/{id}/status")
        @Operation(summary = "Update appointment status", description = "Updates the status of an appointment")
        public ResponseEntity<AppointmentResponse> updateAppointmentStatus(
                        @PathVariable Long id,
                        @RequestParam Appointment.AppointmentStatus status) {
                log.info("Received request to update appointment {} status to {}", id, status);

                AppointmentResponse response = appointmentService.updateAppointmentStatus(id, status);

                if (response != null) {
                        return ResponseEntity.ok(response);
                } else {
                        return ResponseEntity.notFound().build();
                }
        }

        @GetMapping("/eligible-patients")
        @Operation(summary = "Get eligible patients", description = "Retrieves list of patients eligible for appointment booking (registered within 3 days)")
        public ResponseEntity<List<Patient>> getEligiblePatients() {
                log.info("Received request to get eligible patients");

                List<Patient> eligiblePatients = patientEligibilityService.getAllEligiblePatients();
                return ResponseEntity.ok(eligiblePatients);
        }

        @GetMapping("/patient/{patientId}/eligible")
        @Operation(summary = "Check patient eligibility", description = "Checks if a patient is eligible for appointment booking")
        public ResponseEntity<Boolean> checkPatientEligibility(@PathVariable Long patientId) {
                log.info("Received request to check eligibility for patient: {}", patientId);

                boolean isEligible = patientEligibilityService.isPatientEligible(patientId);
                return ResponseEntity.ok(isEligible);
        }
}
