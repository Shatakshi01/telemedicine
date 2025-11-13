package com.appointment.repository;

import com.appointment.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {

    @Query("SELECT p FROM Patient p WHERE p.createdAt >= :threeDaysAgo")
    List<Patient> findEligiblePatients(@Param("threeDaysAgo") LocalDateTime threeDaysAgo);

    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Patient p WHERE p.patientId = :patientId AND p.createdAt >= :threeDaysAgo")
    boolean isPatientEligible(@Param("patientId") Long patientId, @Param("threeDaysAgo") LocalDateTime threeDaysAgo);
}
