package edu.cit.riconalla.dentalink.features.appointments.service;

import edu.cit.riconalla.dentalink.features.appointments.entity.Appointment;
import edu.cit.riconalla.dentalink.features.appointments.entity.AppointmentStatus;
import edu.cit.riconalla.dentalink.features.auth.entity.User;
import edu.cit.riconalla.dentalink.features.appointments.repository.AppointmentRepository;
import edu.cit.riconalla.dentalink.features.dentists.repository.DentistRepository;
import edu.cit.riconalla.dentalink.features.payments.entity.PaymentStatus;
import edu.cit.riconalla.dentalink.features.payments.repository.PaymentRepository;
import edu.cit.riconalla.dentalink.features.services.repository.ServiceRepository;
import edu.cit.riconalla.dentalink.features.auth.repository.UserRepository;
import org.springframework.stereotype.Component;
import edu.cit.riconalla.dentalink.features.appointments.dto.AppointmentResponse;
import edu.cit.riconalla.dentalink.shared.exception.BookingConflictException;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Component
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final ServiceRepository serviceRepository;
    private final DentistRepository dentistRepository;
    private final PaymentRepository paymentRepository;

    public AppointmentService(AppointmentRepository appointmentRepository,
                              UserRepository userRepository,
                              ServiceRepository serviceRepository,
                              DentistRepository dentistRepository,
                              PaymentRepository paymentRepository) {
        this.appointmentRepository = appointmentRepository;
        this.userRepository = userRepository;
        this.serviceRepository = serviceRepository;
        this.dentistRepository = dentistRepository;
        this.paymentRepository = paymentRepository;
    }

    /** Patient books an appointment */
    public Appointment createAppointment(String patientEmail, Long serviceId, Long dentistId,
                                         LocalDateTime appointmentDatetime) {
        // ── Datetime business-rule guards (fail fast before any DB queries) ──────
        if (appointmentDatetime.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Appointment date must be in the future");
        }
        if (appointmentDatetime.getDayOfWeek() == DayOfWeek.SUNDAY) {
            throw new IllegalArgumentException("The clinic is closed on Sundays");
        }
        LocalTime bookingTime = appointmentDatetime.toLocalTime();
        boolean isSaturday = appointmentDatetime.getDayOfWeek() == DayOfWeek.SATURDAY;
        LocalTime open  = isSaturday ? LocalTime.of(9, 0) : LocalTime.of(8, 0);
        LocalTime close = LocalTime.of(17, 0);
        if (bookingTime.isBefore(open) || bookingTime.isAfter(close)) {
            String hours = isSaturday ? "9:00 AM – 5:00 PM" : "8:00 AM – 5:00 PM";
            throw new IllegalArgumentException(
                    "Appointments must be scheduled during clinic operating hours (" + hours + ")");
        }

        // Validate patient exists
        User patient = userRepository.findByEmail(patientEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Validate service exists
        serviceRepository.findById(serviceId)
                .orElseThrow(() -> new RuntimeException("Service not found"));

        // Validate dentist exists and is active
        var dentist = dentistRepository.findById(dentistId)
                .orElseThrow(() -> new RuntimeException("Dentist not found"));
        if (!"ACTIVE".equals(dentist.getDentistStatus())) {
            throw new IllegalArgumentException("Dentist is not currently available for booking");
        }

        // Double-booking check (exclude CANCELLED — those slots are free again)
        if (appointmentRepository.existsByDentistIdAndAppointmentDatetimeAndAppointmentStatusNot(
                dentistId, appointmentDatetime, AppointmentStatus.CANCELLED)) {
            throw new BookingConflictException("Conflict: This dentist is already booked at the selected date and time");
        }

        Appointment a = Appointment.builder()
                .patientId(patient.getUserId())
                .serviceId(serviceId)
                .dentistId(dentistId)
                .appointmentDatetime(appointmentDatetime)
                .appointmentStatus(AppointmentStatus.PENDING_PAYMENT)
                .paymentStatus(PaymentStatus.UNPAID)
                .build();

        return appointmentRepository.save(a);
    }

    /** Patient gets their own appointments */
    public List<Appointment> getPatientAppointments(String patientEmail) {
        User patient = userRepository.findByEmail(patientEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return appointmentRepository.findByPatientId(patient.getUserId());
    }

    /** Admin gets all appointments, optionally filtered by status */
    public List<Appointment> getAllAppointments(String status) {
        if (status != null && !status.isBlank()) {
            AppointmentStatus s = AppointmentStatus.valueOf(status.toUpperCase());
            return appointmentRepository.findByAppointmentStatus(s);
        }
        return appointmentRepository.findAll();
    }

    /** Patient cancels their own unpaid appointment */
    public Appointment cancelOwnAppointment(String patientEmail, Long appointmentId) {
        Appointment a = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        User patient = userRepository.findByEmail(patientEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (!a.getPatientId().equals(patient.getUserId())) {
            throw new RuntimeException("Access denied: you can only cancel your own appointments");
        }
        if (a.getPaymentStatus() != PaymentStatus.UNPAID) {
            throw new IllegalArgumentException("Only unpaid appointments can be cancelled");
        }

        a.setAppointmentStatus(AppointmentStatus.CANCELLED);
        return appointmentRepository.save(a);
    }

    public Appointment getAppointmentById(Long id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));
    }

    public Appointment getAppointmentByIdForCaller(Long id, String callerEmail, boolean isAdmin) {
        Appointment appointment = getAppointmentById(id);

        if (!isAdmin) {
            User caller = userRepository.findByEmail(callerEmail)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            if (!appointment.getPatientId().equals(caller.getUserId())) {
                throw new RuntimeException("Access denied: you can only view your own appointments");
            }
        }

        return appointment;
    }

    private static final java.util.Set<String> ADMIN_ALLOWED_STATUSES =
            java.util.Set.of("COMPLETED", "CANCELLED");

    /** Admin updates status to COMPLETED or CANCELLED */
    public Appointment updateStatus(Long id, String status) {
        if (status == null || !ADMIN_ALLOWED_STATUSES.contains(status.toUpperCase())) {
            throw new IllegalArgumentException(
                "Invalid status. Admin may only set: COMPLETED, CANCELLED");
        }
        Appointment a = getAppointmentById(id);
        if ("COMPLETED".equalsIgnoreCase(status) && a.getPaymentStatus() != PaymentStatus.PAID) {
            throw new IllegalArgumentException(
                "Cannot mark an unpaid appointment as completed.");
        }
        a.setAppointmentStatus(AppointmentStatus.valueOf(status.toUpperCase()));
        return appointmentRepository.save(a);
    }

    /** Admin dashboard stats */
    public java.util.Map<String, Object> getDashboardStats() {
        List<Appointment> all = appointmentRepository.findAll();
        long total          = all.size();
        long pending        = all.stream().filter(a -> a.getPaymentStatus() == PaymentStatus.UNPAID).count();
        long confirmed      = all.stream().filter(a -> a.getAppointmentStatus() == AppointmentStatus.CONFIRMED).count();
        long completed      = all.stream().filter(a -> a.getAppointmentStatus() == AppointmentStatus.COMPLETED).count();
        long cancelled      = all.stream().filter(a -> a.getAppointmentStatus() == AppointmentStatus.CANCELLED).count();
        long pendingPayAppt = all.stream().filter(a -> a.getAppointmentStatus() == AppointmentStatus.PENDING_PAYMENT).count();

        // Revenue from payments table (payment_amount) — not service price
        java.math.BigDecimal revenue = paymentRepository.findAll().stream()
                .filter(p -> "PAID".equals(p.getPaymentStatus()))
                .map(p -> p.getPaymentAmount() != null ? p.getPaymentAmount() : java.math.BigDecimal.ZERO)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

        // Recent appointments — U-3 locked shape: {id, patientName, dentistName, appointmentDatetime, appointmentStatus}
        List<java.util.Map<String, Object>> recent = all.stream()
                .sorted((a, b) -> b.getAppointmentId().compareTo(a.getAppointmentId()))
                .limit(5)
                .map(a -> {
                    String patientName = userRepository.findById(a.getPatientId())
                            .map(u -> u.getFirstName() + " " + u.getLastName())
                            .orElse("Unknown");
                    String dentistName = dentistRepository.findById(a.getDentistId())
                            .map(d -> d.getDentistName())
                            .orElse("Unknown");
                    String serviceName = serviceRepository.findById(a.getServiceId())
                            .map(s -> s.getServiceName())
                            .orElse("Unknown Service");
                    java.util.Map<String, Object> item = new java.util.LinkedHashMap<>();
                    item.put("id", a.getAppointmentId());
                    item.put("patientName", patientName);
                    item.put("serviceName", serviceName);
                    item.put("dentistName", dentistName);
                    item.put("appointmentDatetime", a.getAppointmentDatetime());
                    item.put("appointmentStatus", a.getAppointmentStatus());
                    return item;
                })
                .collect(java.util.stream.Collectors.toList());

        return java.util.Map.of(
                "totalAppointments",          total,
                "pendingPayments",            pending,
                "confirmedAppointments",      confirmed,
                "completedAppointments",      completed,
                "cancelledAppointments",      cancelled,
                "pendingPaymentAppointments", pendingPayAppt,
                "totalRevenue",               revenue,
                "recentAppointments",         recent
        );
    }

    public AppointmentResponse toResponse(Appointment a) {
        AppointmentResponse r = new AppointmentResponse();

        r.setId(a.getAppointmentId());
        r.setServiceId(a.getServiceId());
        r.setDentistId(a.getDentistId());
        r.setAppointmentDatetime(a.getAppointmentDatetime());
        r.setStatus(a.getAppointmentStatus());
        r.setPaymentStatus(a.getPaymentStatus());

        serviceRepository.findById(a.getServiceId()).ifPresent(s -> {
            r.setServiceName(s.getServiceName());
            r.setServicePrice(s.getServicePrice());
            r.setServiceImageUrl(s.getServiceImageUrl());
        });

        dentistRepository.findById(a.getDentistId()).ifPresent(d -> {
            r.setDentistName(d.getDentistName());
            r.setDentistSpecialization(d.getDentistSpecialization());
        });

        java.util.Map<String, Object> patientMap = new java.util.LinkedHashMap<>();
        userRepository.findById(a.getPatientId()).ifPresentOrElse(u -> {
            patientMap.put("id",        u.getUserId());
            patientMap.put("firstName", u.getFirstName()  != null ? u.getFirstName()  : "");
            patientMap.put("lastName",  u.getLastName()   != null ? u.getLastName()   : "");
            patientMap.put("email",     u.getEmail()      != null ? u.getEmail()      : "");
        }, () -> {
            patientMap.put("id",        a.getPatientId());
            patientMap.put("firstName", "Unknown");
            patientMap.put("lastName",  "Patient");
            patientMap.put("email",     "");
        });
        r.setPatient(patientMap);

        return r;
    }

}