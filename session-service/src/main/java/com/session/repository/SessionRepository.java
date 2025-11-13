package com.session.repository;

import com.session.entity.Session;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SessionRepository extends MongoRepository<Session, String> {

    Optional<Session> findByAppointmentId(Long appointmentId);

    List<Session> findByPatientId(Long patientId);

    List<Session> findByDoctorId(Long doctorId);

    List<Session> findByStatus(Session.SessionStatus status);

    List<Session> findByPatientIdAndStatus(Long patientId, Session.SessionStatus status);

    List<Session> findByDoctorIdAndStatus(Long doctorId, Session.SessionStatus status);
}
