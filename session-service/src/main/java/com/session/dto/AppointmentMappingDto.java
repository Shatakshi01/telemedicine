package com.session.dto;

import com.session.entity.AppointmentMapping;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentMappingDto {

    private String id;
    private Long appointmentId;
    private Long patientId;
    private Long doctorId;
    private LocalDateTime appointmentTime;
    private String appointmentType;
    private AppointmentMapping.AppointmentStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
