package com.appointment.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PatientRegisteredEvent {
    // Base event fields
    private String eventId;
    private String eventType;
    private LocalDateTime timestamp;
    private String source;
    private String version;

    // Patient specific fields
    private Long patientId;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;

    @JsonFormat(shape = JsonFormat.Shape.ARRAY)
    private LocalDate dateOfBirth;

    private String gender;
    private String address;

    @JsonFormat(shape = JsonFormat.Shape.ARRAY)
    private LocalDateTime registeredAt;
}