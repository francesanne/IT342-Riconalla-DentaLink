package edu.cit.riconalla.dentalink.controller;

import edu.cit.riconalla.dentalink.entity.Appointment;
import edu.cit.riconalla.dentalink.entity.Dentist;
import edu.cit.riconalla.dentalink.entity.Service;
import edu.cit.riconalla.dentalink.entity.User;
import edu.cit.riconalla.dentalink.repository.DentistRepository;
import edu.cit.riconalla.dentalink.repository.ServiceRepository;
import edu.cit.riconalla.dentalink.repository.UserRepository;
import edu.cit.riconalla.dentalink.service.AppointmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final UserRepository userRepository;
    private final ServiceRepository serviceRepository;
    private final DentistRepository dentistRepository;

    public AppointmentController(AppointmentService appointmentService,
                                 UserRepository userRepository,
                                 ServiceRepository serviceRepository,
                                 DentistRepository dentistRepository) {
        this.appointmentService = appointmentService;
        this.userRepository = userRepository;
        this.serviceRepository = serviceRepository;
        this.dentistRepository = dentistRepository;
    }

    /** POST /appointments — Patient books */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createAppointment(@RequestBody Map<String, Object> body,
                                                                 Authentication auth) {
        Long serviceId = Long.valueOf(body.get("serviceId").toString());
        Long dentistId = Long.valueOf(body.get("dentistId").toString());
        String dtStr = (String) body.get("appointmentDatetime");
        LocalDateTime dt = LocalDateTime.parse(dtStr);

        Appointment a = appointmentService.createAppointment(auth.getName(), serviceId, dentistId, dt);
        return ResponseEntity.status(201).body(Map.of("success", true, "data", enrichAppointment(a)));
    }

    /** GET /appointments — ADMIN gets all, PATIENT gets their own */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAppointments(
            @RequestParam(required = false) String status,
            Authentication auth) {

        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        List<Appointment> appointments;
        if (isAdmin) {
            appointments = appointmentService.getAllAppointments(status);
        } else {
            appointments = appointmentService.getPatientAppointments(auth.getName());
        }

        List<Map<String, Object>> enriched = appointments.stream()
                .map(this::enrichAppointment)
                .collect(Collectors.toList());

        return ResponseEntity.ok(Map.of("success", true, "data", enriched));
    }

    /** GET /appointments/{id} */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getAppointment(@PathVariable Long id, Authentication auth) {
        Appointment a = appointmentService.getAppointmentById(id);

        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(ga -> ga.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin) {
            User patient = userRepository.findByEmail(auth.getName()).orElseThrow();
            if (!a.getPatientId().equals(patient.getUserId())) {
                throw new RuntimeException("Forbidden: Cannot access this appointment");
            }
        }

        return ResponseEntity.ok(Map.of("success", true, "data", enrichAppointment(a)));
    }

    /** PUT /appointments/{id}/status — Admin only */
    @PutMapping("/{id}/status")
    public ResponseEntity<Map<String, Object>> updateStatus(@PathVariable Long id,
                                                            @RequestBody Map<String, Object> body,
                                                            Authentication auth) {
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin) throw new RuntimeException("Forbidden: Admin only");

        String status = (String) body.get("status");
        Appointment a = appointmentService.updateStatus(id, status);
        return ResponseEntity.ok(Map.of("success", true, "data", enrichAppointment(a)));
    }

    // Enriches appointment with service name, dentist name, patient info
    private Map<String, Object> enrichAppointment(Appointment a) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", a.getAppointmentId());
        map.put("serviceId", a.getServiceId());
        map.put("dentistId", a.getDentistId());
        map.put("appointmentDatetime", a.getAppointmentDatetime());
        map.put("status", a.getAppointmentStatus());
        map.put("paymentStatus", a.getPaymentStatus());

        serviceRepository.findById(a.getServiceId()).ifPresent(s -> {
            map.put("serviceName", s.getServiceName());
            map.put("servicePrice", s.getServicePrice());
            map.put("serviceImageUrl", s.getServiceImageUrl());
        });

        dentistRepository.findById(a.getDentistId()).ifPresent(d -> {
            map.put("dentistName", d.getDentistName());
            map.put("dentistSpecialization", d.getDentistSpecialization());
        });

        userRepository.findById(a.getPatientId()).ifPresent(u -> {
            Map<String, Object> patient = new LinkedHashMap<>();
            patient.put("id", u.getUserId());
            patient.put("firstName", u.getFirstName());
            patient.put("lastName", u.getLastName());
            patient.put("email", u.getEmail());
            map.put("patient", patient);
        });

        return map;
    }
}