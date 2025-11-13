package com.session.controller;

import com.session.dto.AppointmentMappingDto;
import com.session.entity.AppointmentMapping;
import com.session.service.AppointmentMappingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/appointments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Appointment Mappings", description = "APIs for managing appointment-doctor mappings")
public class AppointmentMappingController {

    private final AppointmentMappingService appointmentMappingService;

    @GetMapping("/{appointmentId}")
    @Operation(summary = "Get appointment mapping", description = "Retrieve appointment mapping by appointment ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Appointment mapping found"),
            @ApiResponse(responseCode = "404", description = "Appointment mapping not found")
    })
    public ResponseEntity<AppointmentMappingDto> getAppointmentMapping(
            @Parameter(description = "Appointment ID") @PathVariable Long appointmentId) {

        log.info("REST API: Fetching appointment mapping for appointment ID: {}", appointmentId);
        Optional<AppointmentMappingDto> mapping = appointmentMappingService.findByAppointmentId(appointmentId);

        return mapping.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/patient/{patientId}")
    @Operation(summary = "Get appointments by patient", description = "Retrieve all appointment mappings for a specific patient")
    @ApiResponse(responseCode = "200", description = "Appointment mappings retrieved successfully")
    public ResponseEntity<List<AppointmentMappingDto>> getAppointmentsByPatient(
            @Parameter(description = "Patient ID") @PathVariable Long patientId) {

        log.info("REST API: Fetching appointment mappings for patient ID: {}", patientId);
        List<AppointmentMappingDto> mappings = appointmentMappingService.findByPatientId(patientId);
        return ResponseEntity.ok(mappings);
    }

    @PostMapping("/{appointmentId}/status")
    @Operation(summary = "Update appointment status", description = "Update the status of an appointment mapping")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status updated successfully"),
            @ApiResponse(responseCode = "404", description = "Appointment mapping not found")
    })
    public ResponseEntity<AppointmentMappingDto> updateAppointmentStatus(
            @Parameter(description = "Appointment ID") @PathVariable Long appointmentId,
            @RequestParam AppointmentMapping.AppointmentStatus status) {

        try {
            log.info("REST API: Updating appointment status for appointment ID: {} to {}", appointmentId, status);
            AppointmentMappingDto updatedMapping = appointmentMappingService.updateMappingStatus(appointmentId, status);
            return ResponseEntity.ok(updatedMapping);
        } catch (RuntimeException e) {
            log.error("Error updating appointment status: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping
    @Operation(summary = "Get all appointment mappings", description = "Retrieve all appointment mappings (for monitoring)")
    @ApiResponse(responseCode = "200", description = "All appointment mappings retrieved successfully")
    public ResponseEntity<List<AppointmentMappingDto>> getAllAppointmentMappings() {

        log.info("REST API: Fetching all appointment mappings");
        List<AppointmentMappingDto> mappings = appointmentMappingService.findAll();
        return ResponseEntity.ok(mappings);
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get appointments by status", description = "Retrieve all appointment mappings with a specific status")
    @ApiResponse(responseCode = "200", description = "Appointment mappings retrieved successfully")
    public ResponseEntity<List<AppointmentMappingDto>> getAppointmentsByStatus(
            @Parameter(description = "Appointment status") @PathVariable AppointmentMapping.AppointmentStatus status) {

        log.info("REST API: Fetching appointment mappings with status: {}", status);
        List<AppointmentMappingDto> mappings = appointmentMappingService.findByStatus(status);
        return ResponseEntity.ok(mappings);
    }
}
