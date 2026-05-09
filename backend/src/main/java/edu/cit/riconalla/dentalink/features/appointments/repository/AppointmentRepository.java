package edu.cit.riconalla.dentalink.features.appointments.repository;

import edu.cit.riconalla.dentalink.features.appointments.entity.Appointment;
import edu.cit.riconalla.dentalink.features.appointments.entity.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    boolean existsByDentistIdAndAppointmentDatetime(Long dentistId, LocalDateTime appointmentDatetime);

    List<Appointment> findByPatientId(Long patientId);

    List<Appointment> findByAppointmentStatus(AppointmentStatus status);

    Optional<Appointment> findByPaymongoIntentId(String paymongoIntentId);
}