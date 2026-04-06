package edu.cit.riconalla.dentalink.controller;

import edu.cit.riconalla.dentalink.entity.User;
import edu.cit.riconalla.dentalink.repository.UserRepository;
import edu.cit.riconalla.dentalink.service.AppointmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class AdminController {

    private final AppointmentService appointmentService;
    private final UserRepository userRepository;

    public AdminController(AppointmentService appointmentService, UserRepository userRepository) {
        this.appointmentService = appointmentService;
        this.userRepository = userRepository;
    }

    /** GET /admin/dashboard */
    @GetMapping("/admin/dashboard")
    public ResponseEntity<Map<String, Object>> dashboard(Authentication auth) {
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin) throw new RuntimeException("Forbidden: Admin only");

        Map<String, Object> stats = appointmentService.getDashboardStats();
        return ResponseEntity.ok(Map.of("success", true, "data", stats));
    }

    /** GET /auth/me */
    @GetMapping("/auth/me")
    public ResponseEntity<Map<String, Object>> me(Authentication auth) {
        User user = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Map<String, Object> data = Map.of(
                "id", user.getUserId(),
                "firstName", user.getFirstName(),
                "lastName", user.getLastName(),
                "email", user.getEmail(),
                "role", user.getRole(),
                "profileImageUrl", user.getProfileImageUrl() != null ? user.getProfileImageUrl() : ""
        );
        return ResponseEntity.ok(Map.of("success", true, "data", data));
    }

    /** POST /auth/logout */
    @PostMapping("/auth/logout")
    public ResponseEntity<Map<String, Object>> logout() {
        return ResponseEntity.ok(Map.of("success", true, "data", "Logged out successfully"));
    }
}