package edu.cit.riconalla.dentalink.features.appointments.controller;

import edu.cit.riconalla.dentalink.features.appointments.dto.AppointmentResponse;
import edu.cit.riconalla.dentalink.features.appointments.dto.CreateAppointmentRequest;
import edu.cit.riconalla.dentalink.features.appointments.entity.Appointment;
import edu.cit.riconalla.dentalink.features.appointments.service.AppointmentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @PreAuthorize("hasAuthority('PATIENT')")
    @PostMapping
    public ResponseEntity<Map<String, Object>> createAppointment(@Valid @RequestBody CreateAppointmentRequest request,
                                                                 Authentication auth) {
        Long serviceId = request.getServiceId();
        Long dentistId = request.getDentistId();
        LocalDateTime dt = LocalDateTime.parse(request.getAppointmentDatetime());

        var a = appointmentService.createAppointment(auth.getName(), serviceId, dentistId, dt);

        return ResponseEntity.status(201)
                .body(Map.of("success", true, "data", appointmentService.toResponse(a)));
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAppointments(
            @RequestParam(required = false) String status,
            Authentication auth) {

        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ADMIN"));

        List<Appointment> appointments = isAdmin
                ? appointmentService.getAllAppointments(status)
                : appointmentService.getPatientAppointments(auth.getName());

        List<AppointmentResponse> enriched = appointments.stream()
                .map(appointmentService::toResponse)
                .toList();

        return ResponseEntity.ok(Map.of("success", true, "data", enriched));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getAppointment(@PathVariable Long id,
                                                              Authentication auth) {
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ADMIN"));

        Appointment a = appointmentService.getAppointmentByIdForCaller(id, auth.getName(), isAdmin);

        return ResponseEntity.ok(Map.of("success", true, "data", appointmentService.toResponse(a)));
    }

    @PreAuthorize("hasAuthority('PATIENT')")
    @PostMapping("/{id}/cancel")
    public ResponseEntity<Map<String, Object>> cancelAppointment(@PathVariable Long id,
                                                                 Authentication auth) {
        var a = appointmentService.cancelOwnAppointment(auth.getName(), id);
        return ResponseEntity.ok(Map.of("success", true, "data", appointmentService.toResponse(a)));
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PutMapping("/{id}/status")
    public ResponseEntity<Map<String, Object>> updateStatus(@PathVariable Long id,
                                                            @RequestBody Map<String, Object> body) {

        String status = (String) body.get("status");
        Appointment a = appointmentService.updateStatus(id, status);

        return ResponseEntity.ok(Map.of("success", true, "data", appointmentService.toResponse(a)));
    }
}