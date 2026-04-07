package edu.cit.riconalla.dentalink.controller;

import edu.cit.riconalla.dentalink.dto.LoginRequest;
import edu.cit.riconalla.dentalink.dto.LoginResponse;
import edu.cit.riconalla.dentalink.dto.RegisterRequest;
import edu.cit.riconalla.dentalink.service.UserService;
import org.springframework.web.bind.annotation.*;
import edu.cit.riconalla.dentalink.dto.GoogleLoginRequest;
import edu.cit.riconalla.dentalink.service.GoogleService;
import edu.cit.riconalla.dentalink.strategy.EmailPasswordStrategy;
import edu.cit.riconalla.dentalink.strategy.GoogleStrategy;
import edu.cit.riconalla.dentalink.service.AuthService;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;
    private final AuthService authService;
    private final EmailPasswordStrategy emailStrategy;
    private final GoogleStrategy googleStrategy;

    public AuthController(UserService userService,
                          AuthService authService,
                          EmailPasswordStrategy emailStrategy,
                          GoogleStrategy googleStrategy) {
        this.userService = userService;
        this.authService = authService;
        this.emailStrategy = emailStrategy;
        this.googleStrategy = googleStrategy;
    }

    @PostMapping("/register")
    public String register(@RequestBody RegisterRequest request) {

        userService.registerUser(request);

        return "User registered successfully";
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {

        String token = authService.authenticate(emailStrategy, request);

        return new LoginResponse(token);
    }

    @PostMapping("/google")
    public LoginResponse googleLogin(@RequestBody GoogleLoginRequest request) {

        String token = authService.authenticate(googleStrategy, request);

        return new LoginResponse(token);
    }
}