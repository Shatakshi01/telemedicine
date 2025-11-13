package com.patient.dto;

import com.patient.entity.Patient;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class PatientResponseDto {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private LocalDate dateOfBirth;
    private Patient.Gender gender;
    private String address;
    private String medicalHistory;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
