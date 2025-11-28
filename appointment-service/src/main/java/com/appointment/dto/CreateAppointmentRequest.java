package com.appointment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "Request to create a new appointment")
public class CreateAppointmentRequest {

    @NotNull(message = "Patient ID is required")
    @Schema(description = "ID of the patient", example = "1", required = true)
    private Long patientId;

    @NotNull(message = "Doctor ID is required")
    @Schema(description = "ID of the doctor", example = "2", required = true)
    private Long doctorId;

    @NotNull(message = "Appointment date is required")
    @Future(message = "Appointment date must be in the future")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    @Schema(description = "Appointment date and time", example = "2025-12-25 10:30", required = false)
    private LocalDateTime appointmentDate;

    @Schema(description = "Reason for the appointment", example = "Regular checkup")
    private String reason;

    @Schema(description = "Additional notes", example = "Patient has high blood pressure")
    private String notes;
}
