package edu.cit.riconalla.dentalink.service;

import edu.cit.riconalla.dentalink.entity.*;
import edu.cit.riconalla.dentalink.repository.AppointmentRepository;
import edu.cit.riconalla.dentalink.repository.DentistRepository;
import edu.cit.riconalla.dentalink.repository.ServiceRepository;
import edu.cit.riconalla.dentalink.repository.UserRepository;
import org.springframework.stereotype.Component;
import edu.cit.riconalla.dentalink.dto.AppointmentResponse;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final ServiceRepository serviceRepository;
    private final DentistRepository dentistRepository;

    public AppointmentService(AppointmentRepository appointmentRepository,
                              UserRepository userRepository,
                              ServiceRepository serviceRepository,
                              DentistRepository dentistRepository) {
        this.appointmentRepository = appointmentRepository;
        this.userRepository = userRepository;
        this.serviceRepository = serviceRepository;
        this.dentistRepository = dentistRepository;
    }

    /** Patient books an appointment */
    public Appointment createAppointment(String patientEmail, Long serviceId, Long dentistId,
                                         LocalDateTime appointmentDatetime) {
        // Validate patient exists
        User patient = userRepository.findByEmail(patientEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Validate service and dentist exist
        serviceRepository.findById(serviceId)
                .orElseThrow(() -> new RuntimeException("Service not found"));
        dentistRepository.findById(dentistId)
                .orElseThrow(() -> new RuntimeException("Dentist not found"));

        // Double-booking check
        if (appointmentRepository.existsByDentistIdAndAppointmentDatetime(dentistId, appointmentDatetime)) {
            throw new RuntimeException("Conflict: This dentist is already booked at the selected date and time");
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

    public Appointment getAppointmentById(Long id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));
    }

    /** Admin updates status to COMPLETED or CANCELLED */
    public Appointment updateStatus(Long id, String status) {
        if (status.equalsIgnoreCase("CONFIRMED")) {
            throw new RuntimeException("Forbidden: Only PayMongo webhook can set CONFIRMED status");
        }
        Appointment a = getAppointmentById(id);
        a.setAppointmentStatus(AppointmentStatus.valueOf(status.toUpperCase()));
        return appointmentRepository.save(a);
    }

    /** Admin dashboard stats */
    public java.util.Map<String, Object> getDashboardStats() {
        List<Appointment> all = appointmentRepository.findAll();
        long total = all.size();
        long pending = all.stream().filter(a -> a.getPaymentStatus() == PaymentStatus.UNPAID).count();
        long confirmed = all.stream().filter(a -> a.getAppointmentStatus() == AppointmentStatus.CONFIRMED).count();

        java.math.BigDecimal revenue = all.stream()
                .filter(a -> a.getPaymentStatus() == PaymentStatus.PAID)
                .map(a -> {
                    try {
                        return serviceRepository.findById(a.getServiceId())
                                .map(edu.cit.riconalla.dentalink.entity.Service::getServicePrice)
                                .orElse(java.math.BigDecimal.ZERO);
                    } catch (Exception e) {
                        return java.math.BigDecimal.ZERO;
                    }
                })
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

        List<Appointment> recent = all.stream()
                .sorted((a, b) -> b.getAppointmentId().compareTo(a.getAppointmentId()))
                .limit(5)
                .collect(java.util.stream.Collectors.toList());

        return java.util.Map.of(
                "totalAppointments", total,
                "pendingPayments", pending,
                "confirmedAppointments", confirmed,
                "totalRevenue", revenue,
                "recentAppointments", recent
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

        userRepository.findById(a.getPatientId()).ifPresent(u -> {
            java.util.Map<String, Object> patient = new java.util.LinkedHashMap<>();
            patient.put("id", u.getUserId());
            patient.put("firstName", u.getFirstName());
            patient.put("lastName", u.getLastName());
            patient.put("email", u.getEmail());
            r.setPatient(patient);
        });

        return r;
    }

}