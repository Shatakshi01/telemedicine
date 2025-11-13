package com.session.dto;

import com.session.entity.Session;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionResponseDto {

    private String id;
    private Long appointmentId;
    private Long patientId;
    private Long doctorId;
    private Session.SessionStatus status;
    private LocalDateTime scheduledTime;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String sessionUrl;
    private String notes;
    private Integer fileCount;
    private Boolean hasPatientFiles;
    private Boolean hasDoctorFiles;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
