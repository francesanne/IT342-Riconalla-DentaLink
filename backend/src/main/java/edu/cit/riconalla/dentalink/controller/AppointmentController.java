package edu.cit.riconalla.dentalink.controller;

import edu.cit.riconalla.dentalink.entity.Appointment;
import edu.cit.riconalla.dentalink.service.AppointmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import edu.cit.riconalla.dentalink.dto.AppointmentResponse;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createAppointment(@RequestBody Map<String, Object> body,
                                                                 Authentication auth) {
        Long serviceId = Long.valueOf(body.get("serviceId").toString());
        Long dentistId = Long.valueOf(body.get("dentistId").toString());
        String dtStr = (String) body.get("appointmentDatetime");
        LocalDateTime dt = LocalDateTime.parse(dtStr);

        var a = appointmentService.createAppointment(auth.getName(), serviceId, dentistId, dt);

        return ResponseEntity.status(201)
                .body(Map.of("success", true, "data", appointmentService.toResponse(a)));
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAppointments(
            @RequestParam(required = false) String status,
            Authentication auth) {

        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        List<Appointment> appointments = isAdmin
                ? appointmentService.getAllAppointments(status)
                : appointmentService.getPatientAppointments(auth.getName());

        List<AppointmentResponse> enriched = appointments.stream()
                .map(appointmentService::toResponse)
                .toList();

        return ResponseEntity.ok(Map.of("success", true, "data", enriched));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getAppointment(@PathVariable Long id) {
        Appointment a = appointmentService.getAppointmentById(id);

        return ResponseEntity.ok(Map.of("success", true, "data", appointmentService.toResponse(a)));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Map<String, Object>> updateStatus(@PathVariable Long id,
                                                            @RequestBody Map<String, Object> body,
                                                            Authentication auth) {

        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin) throw new RuntimeException("Forbidden: Admin only");

        String status = (String) body.get("status");
        Appointment a = appointmentService.updateStatus(id, status);

        return ResponseEntity.ok(Map.of("success", true, "data", appointmentService.toResponse(a)));
    }
}