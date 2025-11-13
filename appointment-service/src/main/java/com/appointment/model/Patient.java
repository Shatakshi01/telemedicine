package com.appointment.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "patients", schema = "appointment")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Patient {

    @Id
    private Long patientId;

    @Column(name = "mobile_number")
    private String mobileNumber;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Patient(Long patientId, String mobileNumber) {
        this.patientId = patientId;
        this.mobileNumber = mobileNumber;
    }
}
