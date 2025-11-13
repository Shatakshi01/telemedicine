package com.session.controller;

import com.session.dto.SessionRequestDto;
import com.session.dto.SessionResponseDto;
import com.session.service.SessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/sessions")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Session Management", description = "APIs for managing telemedicine sessions")
public class SessionController {

    private final SessionService sessionService;

    @PostMapping
    @Operation(summary = "Create a new telemedicine session", description = "Creates a new telemedicine session linked to an appointment")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Session created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid session data"),
            @ApiResponse(responseCode = "409", description = "Session already exists for the appointment")
    })
    public ResponseEntity<SessionResponseDto> createSession(
            @Valid @RequestBody SessionRequestDto sessionRequestDto) {

        log.info("REST API: Creating session for appointment ID: {}", sessionRequestDto.getAppointmentId());
        SessionResponseDto response = sessionService.createSession(sessionRequestDto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/{sessionId}/start")
    @Operation(summary = "Start a telemedicine session", description = "Starts a scheduled telemedicine session")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Session started successfully"),
            @ApiResponse(responseCode = "404", description = "Session not found"),
            @ApiResponse(responseCode = "400", description = "Session cannot be started (invalid status)")
    })
    public ResponseEntity<SessionResponseDto> startSession(
            @Parameter(description = "Session ID") @PathVariable String sessionId) {

        log.info("REST API: Starting session with ID: {}", sessionId);
        SessionResponseDto response = sessionService.startSession(sessionId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{sessionId}")
    @Operation(summary = "Get session details", description = "Retrieves details of a specific telemedicine session")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Session found"),
            @ApiResponse(responseCode = "404", description = "Session not found")
    })
    public ResponseEntity<SessionResponseDto> getSession(
            @Parameter(description = "Session ID") @PathVariable String sessionId) {

        log.info("REST API: Fetching session with ID: {}", sessionId);
        SessionResponseDto response = sessionService.getSessionById(sessionId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/patient/{patientId}")
    @Operation(summary = "Get sessions by patient", description = "Retrieves all sessions for a specific patient")
    @ApiResponse(responseCode = "200", description = "Sessions retrieved successfully")
    public ResponseEntity<List<SessionResponseDto>> getSessionsByPatient(
            @Parameter(description = "Patient ID") @PathVariable Long patientId) {

        log.info("REST API: Fetching sessions for patient ID: {}", patientId);
        List<SessionResponseDto> response = sessionService.getSessionsByPatientId(patientId);
        return ResponseEntity.ok(response);
    }
}
