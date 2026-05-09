package edu.cit.riconalla.dentalink.features.payments.repository;

import edu.cit.riconalla.dentalink.features.payments.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByAppointmentAppointmentId(Long appointmentId);

    Optional<Payment> findByPaymongoIntentId(String paymongoIntentId);
}

