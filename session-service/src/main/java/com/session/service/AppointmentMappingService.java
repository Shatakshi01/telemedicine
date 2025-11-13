package com.session.service;

import com.session.dto.AppointmentMappingDto;
import com.session.entity.AppointmentMapping;
import com.session.repository.AppointmentMappingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppointmentMappingService {

    private final AppointmentMappingRepository appointmentMappingRepository;

    public AppointmentMappingDto saveAppointmentMapping(Long appointmentId, Long patientId, Long doctorId,
            String appointmentType, java.time.LocalDateTime appointmentTime) {
        log.info("Saving appointment mapping for appointment ID: {}", appointmentId);

        // Check if mapping already exists
        Optional<AppointmentMapping> existingMapping = appointmentMappingRepository.findByAppointmentId(appointmentId);
        if (existingMapping.isPresent()) {
            log.warn("Appointment mapping already exists for appointment ID: {}", appointmentId);
            return convertToDto(existingMapping.get());
        }

        AppointmentMapping mapping = AppointmentMapping.builder()
                .appointmentId(appointmentId)
                .patientId(patientId)
                .doctorId(doctorId)
                .appointmentType(appointmentType)
                .appointmentTime(appointmentTime)
                .status(AppointmentMapping.AppointmentStatus.PENDING)
                .build();

        AppointmentMapping savedMapping = appointmentMappingRepository.save(mapping);
        log.info("Successfully saved appointment mapping with ID: {}", savedMapping.getId());

        return convertToDto(savedMapping);
    }

    public AppointmentMappingDto updateMappingStatus(Long appointmentId, AppointmentMapping.AppointmentStatus status) {
        log.info("Updating appointment mapping status for appointment ID: {} to {}", appointmentId, status);

        AppointmentMapping mapping = appointmentMappingRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new RuntimeException(
                        "Appointment mapping not found for appointment ID: " + appointmentId));

        mapping.setStatus(status);
        AppointmentMapping updatedMapping = appointmentMappingRepository.save(mapping);

        log.info("Successfully updated appointment mapping status");
        return convertToDto(updatedMapping);
    }

    public Optional<AppointmentMappingDto> findByAppointmentId(Long appointmentId) {
        return appointmentMappingRepository.findByAppointmentId(appointmentId)
                .map(this::convertToDto);
    }

    public List<AppointmentMappingDto> findByDoctorId(Long doctorId) {
        List<AppointmentMapping> mappings = appointmentMappingRepository.findByDoctorId(doctorId);
        return mappings.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<AppointmentMappingDto> findByPatientId(Long patientId) {
        List<AppointmentMapping> mappings = appointmentMappingRepository.findByPatientId(patientId);
        return mappings.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public boolean canCreateSession(Long appointmentId) {
        Optional<AppointmentMapping> mapping = appointmentMappingRepository.findByAppointmentId(appointmentId);
        return mapping.isPresent() &&
                (mapping.get().getStatus() == AppointmentMapping.AppointmentStatus.CONFIRMED ||
                        mapping.get().getStatus() == AppointmentMapping.AppointmentStatus.SESSION_READY);
    }

    public List<AppointmentMappingDto> findAll() {
        log.info("Fetching all appointment mappings");
        List<AppointmentMapping> mappings = appointmentMappingRepository.findAll();
        return mappings.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<AppointmentMappingDto> findByStatus(AppointmentMapping.AppointmentStatus status) {
        log.info("Fetching appointment mappings with status: {}", status);
        List<AppointmentMapping> mappings = appointmentMappingRepository.findByStatus(status);
        return mappings.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public java.util.Map<String, Long> getAppointmentCountsByStatus() {
        log.info("Fetching appointment counts by status");
        List<AppointmentMapping> allMappings = appointmentMappingRepository.findAll();

        return allMappings.stream()
                .collect(Collectors.groupingBy(
                        mapping -> mapping.getStatus().name(),
                        Collectors.counting()));
    }

    private AppointmentMappingDto convertToDto(AppointmentMapping mapping) {
        return AppointmentMappingDto.builder()
                .id(mapping.getId())
                .appointmentId(mapping.getAppointmentId())
                .patientId(mapping.getPatientId())
                .doctorId(mapping.getDoctorId())
                .appointmentType(mapping.getAppointmentType())
                .appointmentTime(mapping.getAppointmentTime())
                .status(mapping.getStatus())
                .createdAt(mapping.getCreatedAt())
                .updatedAt(mapping.getUpdatedAt())
                .build();
    }
}
