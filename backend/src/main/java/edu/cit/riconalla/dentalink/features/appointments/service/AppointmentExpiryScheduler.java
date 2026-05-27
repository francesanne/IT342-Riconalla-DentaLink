package edu.cit.riconalla.dentalink.features.appointments.service;

import edu.cit.riconalla.dentalink.features.appointments.entity.Appointment;
import edu.cit.riconalla.dentalink.features.appointments.entity.AppointmentStatus;
import edu.cit.riconalla.dentalink.features.appointments.repository.AppointmentRepository;
import edu.cit.riconalla.dentalink.features.payments.entity.PaymentStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class AppointmentExpiryScheduler {

    private static final int EXPIRY_MINUTES = 60;

    private final AppointmentRepository appointmentRepository;

    public AppointmentExpiryScheduler(AppointmentRepository appointmentRepository) {
        this.appointmentRepository = appointmentRepository;
    }

    // Runs every 5 minutes — cancels PENDING_PAYMENT/UNPAID appointments older than 1 hour
    @Scheduled(fixedRate = 5 * 60 * 1000)
    public void cancelExpiredPendingAppointments() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(EXPIRY_MINUTES);

        List<Appointment> expired = appointmentRepository
                .findByAppointmentStatusAndPaymentStatusAndCreatedAtBefore(
                        AppointmentStatus.PENDING_PAYMENT,
                        PaymentStatus.UNPAID,
                        cutoff);

        if (expired.isEmpty()) return;

        expired.forEach(a -> a.setAppointmentStatus(AppointmentStatus.CANCELLED));
        appointmentRepository.saveAll(expired);

        System.out.println("[Expiry] Auto-cancelled " + expired.size() + " unpaid appointment(s) older than " + EXPIRY_MINUTES + " min.");
    }
}
