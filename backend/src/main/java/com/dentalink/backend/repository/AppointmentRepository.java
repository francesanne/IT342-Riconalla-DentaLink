package com.dentalink.backend.repository;

import com.dentalink.backend.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    boolean existsByDentistIdAndAppointmentDatetime(Long dentistId, LocalDateTime appointmentDatetime);

    List<Appointment> findByPatientId(Long patientId);

}