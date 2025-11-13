package com.appointment.service;

import com.appointment.model.Patient;
import com.appointment.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PatientEligibilityService {

    private final PatientRepository patientRepository;

    public boolean isPatientEligible(Long patientId) {
        LocalDateTime threeDaysAgo = LocalDateTime.now().minusDays(3);
        boolean isEligible = patientRepository.isPatientEligible(patientId, threeDaysAgo);
        log.debug("Patient {} eligibility check: {}", patientId, isEligible);
        return isEligible;
    }

    public void addPatient(Long patientId, String mobileNumber) {
        Patient existingPatient = patientRepository.findById(patientId).orElse(null);
        if (existingPatient == null) {
            Patient patient = new Patient(patientId, mobileNumber);
            patientRepository.save(patient);
            log.info("Patient {} added with mobile: {}", patientId, mobileNumber);
        } else {
            log.info("Patient {} already exists", patientId);
        }
    }

    public List<Patient> getAllEligiblePatients() {
        LocalDateTime threeDaysAgo = LocalDateTime.now().minusDays(3);
        return patientRepository.findEligiblePatients(threeDaysAgo);
    }
}
