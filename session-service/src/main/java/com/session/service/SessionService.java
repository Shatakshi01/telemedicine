package com.session.service;

import com.session.dto.SessionRequestDto;
import com.session.dto.SessionResponseDto;
import com.session.entity.Session;
import com.session.event.SessionStartedEvent;
import com.session.exception.SessionNotFoundException;
import com.session.kafka.SessionEventProducer;
import com.session.mapper.SessionMapper;
import com.session.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SessionService {

    private final SessionRepository sessionRepository;
    private final SessionEventProducer sessionEventProducer;
    private final SessionMapper sessionMapper;
    private final AppointmentMappingService appointmentMappingService;

    public SessionResponseDto createSession(SessionRequestDto requestDto) {
        log.info("Creating new session for appointment ID: {}", requestDto.getAppointmentId());

        // Check if session already exists for this appointment
        if (sessionRepository.findByAppointmentId(requestDto.getAppointmentId()).isPresent()) {
            throw new IllegalStateException(
                    "Session already exists for appointment ID: " + requestDto.getAppointmentId());
        }

        // Verify appointment mapping exists and is in correct status
        if (!appointmentMappingService.canCreateSession(requestDto.getAppointmentId())) {
            throw new IllegalStateException(
                    "Cannot create session for appointment ID: " + requestDto.getAppointmentId() +
                            ". Appointment must be confirmed first.");
        }

        // Convert DTO to entity
        Session session = sessionMapper.toEntity(requestDto);

        // Generate session URL (in real implementation, this would be a proper video
        // conference URL)
        session.setSessionUrl(generateSessionUrl());

        // Initialize file tracking fields
        session.setFileCount(0);
        session.setHasPatientFiles(false);
        session.setHasDoctorFiles(false);

        // Save session
        Session savedSession = sessionRepository.save(session);
        log.info("Session created successfully with ID: {}", savedSession.getId());

        // Update appointment mapping status to SESSION_READY
        appointmentMappingService.updateMappingStatus(
                requestDto.getAppointmentId(),
                com.session.entity.AppointmentMapping.AppointmentStatus.SESSION_READY);

        return sessionMapper.toResponseDto(savedSession);
    }

    public SessionResponseDto startSession(String sessionId) {
        log.info("Starting session with ID: {}", sessionId);

        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new SessionNotFoundException("Session not found with ID: " + sessionId));

        if (session.getStatus() != Session.SessionStatus.SCHEDULED) {
            throw new IllegalStateException("Session cannot be started. Current status: " + session.getStatus());
        }

        // Update session status and start time
        session.setStatus(Session.SessionStatus.STARTED);
        session.setStartTime(LocalDateTime.now());

        Session updatedSession = sessionRepository.save(session);
        log.info("Session started successfully with ID: {}", updatedSession.getId());

        // Publish session started event
        SessionStartedEvent event = SessionStartedEvent.builder()
                .sessionId(updatedSession.getId())
                .appointmentId(updatedSession.getAppointmentId())
                .patientId(updatedSession.getPatientId())
                .doctorId(updatedSession.getDoctorId())
                .sessionUrl(updatedSession.getSessionUrl())
                .startTime(updatedSession.getStartTime())
                .scheduledTime(updatedSession.getScheduledTime())
                .build();

        sessionEventProducer.publishSessionStartedEvent(event);

        return sessionMapper.toResponseDto(updatedSession);
    }

    public SessionResponseDto getSessionById(String id) {
        log.info("Fetching session with ID: {}", id);

        Session session = sessionRepository.findById(id)
                .orElseThrow(() -> new SessionNotFoundException("Session not found with ID: " + id));

        return sessionMapper.toResponseDto(session);
    }

    public List<SessionResponseDto> getSessionsByPatientId(Long patientId) {
        log.info("Fetching sessions for patient ID: {}", patientId);

        List<Session> sessions = sessionRepository.findByPatientId(patientId);
        return sessions.stream()
                .map(sessionMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    public List<SessionResponseDto> getSessionsByDoctorId(Long doctorId) {
        log.info("Fetching sessions for doctor ID: {}", doctorId);

        List<Session> sessions = sessionRepository.findByDoctorId(doctorId);
        return sessions.stream()
                .map(sessionMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    public List<SessionResponseDto> getAllSessions() {
        log.info("Fetching all sessions");

        List<Session> sessions = sessionRepository.findAll();
        return sessions.stream()
                .map(sessionMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    public SessionResponseDto createSessionFromAppointment(Long appointmentId, Long patientId, Long doctorId,
            LocalDateTime scheduledTime) {
        log.info("Creating session from appointment booking - Appointment ID: {}", appointmentId);

        // Check if session already exists for this appointment
        if (sessionRepository.findByAppointmentId(appointmentId).isPresent()) {
            log.info("Session already exists for appointment ID: {}, skipping creation", appointmentId);
            return sessionMapper.toResponseDto(sessionRepository.findByAppointmentId(appointmentId).get());
        }

        SessionRequestDto requestDto = SessionRequestDto.builder()
                .appointmentId(appointmentId)
                .patientId(patientId)
                .doctorId(doctorId)
                .scheduledTime(scheduledTime)
                .notes("Session created automatically from appointment booking")
                .build();

        return createSession(requestDto);
    }

    private String generateSessionUrl() {
        // In a real implementation, this would integrate with a video conferencing
        // service
        // like Zoom, Teams, WebRTC, etc.
        return "https://telemedicine.example.com/session/" + UUID.randomUUID().toString();
    }
}
