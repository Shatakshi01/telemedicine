package com.session.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Document(collection = "appointment_mappings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentMapping {

    @Id
    private String id;

    @NotNull(message = "Appointment ID is required")
    private Long appointmentId;

    @NotNull(message = "Patient ID is required")
    private Long patientId;

    @NotNull(message = "Doctor ID is required")
    private Long doctorId;

    private LocalDateTime appointmentTime;

    private String appointmentType;

    private AppointmentStatus status;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public enum AppointmentStatus {
        PENDING,
        CONFIRMED,
        SESSION_READY,
        COMPLETED,
        CANCELLED
    }
}
