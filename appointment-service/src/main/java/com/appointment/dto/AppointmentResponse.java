package com.appointment.dto;

import com.appointment.model.Appointment;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Appointment response data")
public class AppointmentResponse {

    @Schema(description = "Appointment ID", example = "1")
    private Long id;

    @Schema(description = "Patient ID", example = "1")
    private Long patientId;

    @Schema(description = "Doctor ID", example = "2")
    private Long doctorId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    @Schema(description = "Appointment date and time", example = "2024-12-25 10:30")
    private LocalDateTime appointmentDate;

    @Schema(description = "Appointment status", example = "SCHEDULED")
    private Appointment.AppointmentStatus status;

    @Schema(description = "Reason for appointment", example = "Regular checkup")
    private String reason;

    @Schema(description = "Additional notes", example = "Patient has high blood pressure")
    private String notes;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Creation timestamp", example = "2024-01-01 12:00:00")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Last update timestamp", example = "2024-01-01 12:00:00")
    private LocalDateTime updatedAt;
}
