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
@Tag(name = "Appointment Mappings", description = "Simplified APIs for appointment sessions")
public class AppointmentMappingController {

    private final AppointmentMappingService appointmentMappingService;

    @GetMapping
    @Operation(summary = "Get all appointment sessions", description = "Retrieve all appointment mappings with optional filtering")
    @ApiResponse(responseCode = "200", description = "All appointment sessions retrieved successfully")
    public ResponseEntity<List<AppointmentMappingDto>> getAllAppointmentSessions(
            @Parameter(description = "Patient ID filter (optional)") @RequestParam(required = false) Long patientId,
            @Parameter(description = "Doctor ID filter (optional)") @RequestParam(required = false) Long doctorId,
            @Parameter(description = "Status filter (optional)") @RequestParam(required = false) AppointmentMapping.AppointmentStatus status) {

        log.info("REST API: Fetching appointment sessions with filters - patientId: {}, doctorId: {}, status: {}",
                patientId, doctorId, status);

        List<AppointmentMappingDto> mappings;

        if (patientId != null) {
            mappings = appointmentMappingService.findByPatientId(patientId);
        } else if (doctorId != null) {
            mappings = appointmentMappingService.findByDoctorId(doctorId);
        } else if (status != null) {
            mappings = appointmentMappingService.findByStatus(status);
        } else {
            mappings = appointmentMappingService.findAll();
        }

        return ResponseEntity.ok(mappings);
    }

    @GetMapping("/{appointmentId}")
    @Operation(summary = "Get specific appointment session", description = "Retrieve a specific appointment session by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Appointment session found"),
            @ApiResponse(responseCode = "404", description = "Appointment session not found")
    })
    public ResponseEntity<AppointmentMappingDto> getAppointmentSession(
            @Parameter(description = "Appointment ID") @PathVariable Long appointmentId) {

        log.info("REST API: Fetching appointment session for appointment ID: {}", appointmentId);
        Optional<AppointmentMappingDto> mapping = appointmentMappingService.findByAppointmentId(appointmentId);

        return mapping.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
