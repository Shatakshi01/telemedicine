package com.session.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.session.dto.SessionResponseDto;
import com.session.service.SessionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/sessions")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Session Management", description = "Simplified APIs for managing telemedicine sessions")
public class SessionController {

    private final SessionService sessionService;

    @GetMapping
    @Operation(summary = "Get all sessions", description = "Retrieve all sessions with optional filtering by patient or doctor")
    @ApiResponse(responseCode = "200", description = "Sessions retrieved successfully")
    public ResponseEntity<List<SessionResponseDto>> getAllSessions(
            @Parameter(description = "Patient ID filter (optional)") @RequestParam(required = false) Long patientId,
            @Parameter(description = "Doctor ID filter (optional)") @RequestParam(required = false) Long doctorId) {

        log.info("REST API: Fetching sessions with filters - patientId: {}, doctorId: {}", patientId, doctorId);

        List<SessionResponseDto> sessions;
        if (patientId != null) {
            sessions = sessionService.getSessionsByPatientId(patientId);
        } else if (doctorId != null) {
            sessions = sessionService.getSessionsByDoctorId(doctorId);
        } else {
            sessions = sessionService.getAllSessions();
        }

        return ResponseEntity.ok(sessions);
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

    @PostMapping("/{sessionId}/start")
    @Operation(summary = "Start a session", description = "Starts a scheduled telemedicine session")
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
}
