package com.patient.controller;

import com.patient.dto.PatientRegistrationDto;
import com.patient.dto.PatientResponseDto;
import com.patient.service.PatientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMethod;

@RestController
@RequestMapping("/patients")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Patient Management", description = "APIs for managing patient registration and information")
@CrossOrigin(origins = "*", methods = { RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE,
        RequestMethod.OPTIONS })
public class PatientController {

    private final PatientService patientService;

    @PostMapping
    @Operation(summary = "Register a new patient", description = "Register a new patient in the system and emit a patient.registered event")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Patient registered successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PatientResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "409", description = "Patient already exists", content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<PatientResponseDto> registerPatient(
            @Valid @RequestBody PatientRegistrationDto registrationDto) {

        log.info("Received patient registration request for email: {}", registrationDto.getEmail());

        PatientResponseDto responseDto = patientService.registerPatient(registrationDto);

        log.info("Patient registered successfully with ID: {}", responseDto.getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get patient details", description = "Retrieve patient details by patient ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Patient found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PatientResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Patient not found", content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<PatientResponseDto> getPatientById(
            @Parameter(description = "Patient ID", required = true) @PathVariable Long id) {

        log.info("Received request to get patient with ID: {}", id);

        PatientResponseDto responseDto = patientService.getPatientById(id);

        return ResponseEntity.ok(responseDto);
    }
}
