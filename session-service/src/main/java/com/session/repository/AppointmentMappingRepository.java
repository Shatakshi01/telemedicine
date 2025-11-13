package com.session.repository;

import com.session.entity.AppointmentMapping;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AppointmentMappingRepository extends MongoRepository<AppointmentMapping, String> {

    Optional<AppointmentMapping> findByAppointmentId(Long appointmentId);

    List<AppointmentMapping> findByPatientId(Long patientId);

    List<AppointmentMapping> findByDoctorId(Long doctorId);

    List<AppointmentMapping> findByStatus(AppointmentMapping.AppointmentStatus status);

    List<AppointmentMapping> findByPatientIdAndStatus(Long patientId, AppointmentMapping.AppointmentStatus status);

    List<AppointmentMapping> findByDoctorIdAndStatus(Long doctorId, AppointmentMapping.AppointmentStatus status);
}
