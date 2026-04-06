package edu.cit.riconalla.dentalink.repository;

import edu.cit.riconalla.dentalink.entity.Appointment;
import edu.cit.riconalla.dentalink.entity.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    boolean existsByDentistIdAndAppointmentDatetime(Long dentistId, LocalDateTime appointmentDatetime);

    List<Appointment> findByPatientId(Long patientId);

    List<Appointment> findByAppointmentStatus(AppointmentStatus status);

}