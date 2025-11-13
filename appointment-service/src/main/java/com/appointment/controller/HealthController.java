package com.appointment.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
public class HealthController {

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        log.info("Health check requested");
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "appointment-service");
        response.put("timestamp", LocalDateTime.now());
        response.put("port", "8084");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> info() {
        log.info("Info endpoint requested");
        Map<String, Object> response = new HashMap<>();
        response.put("service", "appointment-service");
        response.put("version", "1.0.0");
        response.put("description", "Telemedicine Appointment Management Service");
        response.put("endpoints", new String[] {
                "POST /appointments - Book appointment",
                "GET /appointments/{id} - Get appointment",
                "GET /appointments/patient/{patientId} - Get patient appointments",
                "PUT /appointments/{id}/status - Update appointment status",
                "GET /appointments/eligible-patients - Get eligible patients",
                "GET /appointments/patient/{patientId}/eligible - Check patient eligibility"
        });
        return ResponseEntity.ok(response);
    }
}
