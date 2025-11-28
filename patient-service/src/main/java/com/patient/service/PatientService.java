package com.patient.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.patient.dto.PatientRegistrationDto;
import com.patient.dto.PatientResponseDto;
import com.patient.entity.Patient;
import com.patient.event.PatientRegisteredEvent;
import com.patient.exception.PatientAlreadyExistsException;
import com.patient.exception.PatientNotFoundException;
import com.patient.kafka.PatientEventProducer;
import com.patient.mapper.PatientMapper;
import com.patient.repository.PatientRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PatientService {

    private final PatientRepository patientRepository;
    private final PatientEventProducer patientEventProducer;
    private final PatientMapper patientMapper;

    public PatientResponseDto registerPatient(PatientRegistrationDto registrationDto) {
        log.info("Registering new patient with email: {}", registrationDto.getEmail());

        if (patientRepository.existsByEmail(registrationDto.getEmail())) {
            throw new PatientAlreadyExistsException(
                    "Patient with email " + registrationDto.getEmail() + " already exists");
        }

        if (patientRepository.existsByPhoneNumber(registrationDto.getPhoneNumber())) {
            throw new PatientAlreadyExistsException(
                    "Patient with phone number " + registrationDto.getPhoneNumber() + " already exists");
        }

        // Convert DTO to entity
        Patient patient = patientMapper.toEntity(registrationDto);

        // Save patient
        Patient savedPatient = patientRepository.save(patient);
        log.info("Patient registered successfully with ID: {}", savedPatient.getId());

        // Publish event
        PatientRegisteredEvent event = PatientRegisteredEvent.builder()
                .patientId(savedPatient.getId())
                .firstName(savedPatient.getFirstName())
                .lastName(savedPatient.getLastName())
                .email(savedPatient.getEmail())
                .phoneNumber(savedPatient.getPhoneNumber())
                .dateOfBirth(savedPatient.getDateOfBirth())
                .gender(savedPatient.getGender().toString())
                .address(savedPatient.getAddress())
                .registeredAt(LocalDateTime.now())
                .build();

        patientEventProducer.publishPatientRegisteredEvent(event);

        return patientMapper.toResponseDto(savedPatient);
    }

    @Transactional(readOnly = true)
    public PatientResponseDto getPatientById(Long id) {
        log.info("Fetching patient with ID: {}", id);

        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new PatientNotFoundException("Patient not found with ID: " + id));

        return patientMapper.toResponseDto(patient);
    }

    @Transactional(readOnly = true)
    public java.util.List<PatientResponseDto> getAllPatients() {
        log.info("Fetching all patients");

        return patientRepository.findAll().stream()
                .map(patientMapper::toResponseDto)
                .collect(java.util.stream.Collectors.toList());
    }
}
