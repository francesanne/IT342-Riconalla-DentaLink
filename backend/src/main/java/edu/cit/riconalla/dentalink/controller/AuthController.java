package edu.cit.riconalla.dentalink.controller;

import edu.cit.riconalla.dentalink.dto.ApiResponse;
import edu.cit.riconalla.dentalink.dto.AuthResponseDto;
import edu.cit.riconalla.dentalink.dto.GoogleLoginRequest;
import edu.cit.riconalla.dentalink.dto.LoginRequest;
import edu.cit.riconalla.dentalink.dto.RegisterRequest;
import edu.cit.riconalla.dentalink.dto.UserDto;
import edu.cit.riconalla.dentalink.service.AuthService;
import edu.cit.riconalla.dentalink.service.UserService;
import edu.cit.riconalla.dentalink.strategy.EmailPasswordStrategy;
import edu.cit.riconalla.dentalink.strategy.GoogleStrategy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;
    private final EmailPasswordStrategy emailStrategy;
    private final GoogleStrategy googleStrategy;

    public AuthController(AuthService authService,
                          UserService userService,
                          EmailPasswordStrategy emailStrategy,
                          GoogleStrategy googleStrategy) {
        this.authService = authService;
        this.userService = userService;
        this.emailStrategy = emailStrategy;
        this.googleStrategy = googleStrategy;
    }

    /** POST /api/v1/auth/register — SDD §5.3 — 201 Created */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponseDto>> register(@RequestBody RegisterRequest request) {
        AuthResponseDto data = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(data));
    }

    /** POST /api/v1/auth/login — SDD §5.3 — 200 OK */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponseDto>> login(@RequestBody LoginRequest request) {
        AuthResponseDto data = authService.authenticate(emailStrategy, request);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    /** POST /api/v1/auth/google — SDD §5.3 — 200 OK */
    @PostMapping("/google")
    public ResponseEntity<ApiResponse<AuthResponseDto>> googleLogin(@RequestBody GoogleLoginRequest request) {
        AuthResponseDto data = authService.authenticate(googleStrategy, request);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    /** GET /api/v1/auth/me — SDD §5.3 — Bearer JWT (any role) */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserDto>> me(Authentication auth) {
        UserDto userDto = userService.getUserByEmail(auth.getName());
        return ResponseEntity.ok(ApiResponse.success(userDto));
    }

    /** POST /api/v1/auth/logout — SDD §5.3 — client-side logout (stateless JWT) */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout() {
        return ResponseEntity.ok(ApiResponse.success("Logged out successfully"));
    }
}