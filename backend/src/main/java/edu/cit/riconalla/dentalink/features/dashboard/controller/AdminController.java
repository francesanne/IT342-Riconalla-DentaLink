package edu.cit.riconalla.dentalink.features.dashboard.controller;

import edu.cit.riconalla.dentalink.shared.dto.ApiResponse;
import edu.cit.riconalla.dentalink.features.appointments.service.AppointmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final AppointmentService appointmentService;

    public AdminController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    /** GET /api/v1/admin/dashboard — SDD §5.3 — ADMIN role required */
    @GetMapping("/dashboard")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> dashboard() {
        Map<String, Object> stats = appointmentService.getDashboardStats();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}