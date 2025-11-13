package com.session.mapper;

import com.session.dto.SessionRequestDto;
import com.session.dto.SessionResponseDto;
import com.session.entity.Session;
import org.springframework.stereotype.Component;

@Component
public class SessionMapper {

    public Session toEntity(SessionRequestDto requestDto) {
        Session session = new Session();
        session.setAppointmentId(requestDto.getAppointmentId());
        session.setPatientId(requestDto.getPatientId());
        session.setDoctorId(requestDto.getDoctorId());
        session.setScheduledTime(requestDto.getScheduledTime());
        session.setNotes(requestDto.getNotes());
        session.setStatus(Session.SessionStatus.SCHEDULED);
        return session;
    }

    public SessionResponseDto toResponseDto(Session session) {
        return SessionResponseDto.builder()
                .id(session.getId())
                .appointmentId(session.getAppointmentId())
                .patientId(session.getPatientId())
                .doctorId(session.getDoctorId())
                .status(session.getStatus())
                .scheduledTime(session.getScheduledTime())
                .startTime(session.getStartTime())
                .endTime(session.getEndTime())
                .sessionUrl(session.getSessionUrl())
                .notes(session.getNotes())
                .fileCount(session.getFileCount() != null ? session.getFileCount() : 0)
                .hasPatientFiles(session.getHasPatientFiles() != null ? session.getHasPatientFiles() : false)
                .hasDoctorFiles(session.getHasDoctorFiles() != null ? session.getHasDoctorFiles() : false)
                .createdAt(session.getCreatedAt())
                .updatedAt(session.getUpdatedAt())
                .build();
    }
}
