package edu.cit.riconalla.dentalink.controller;

import edu.cit.riconalla.dentalink.dto.LoginRequest;
import edu.cit.riconalla.dentalink.dto.LoginResponse;
import edu.cit.riconalla.dentalink.dto.RegisterRequest;
import edu.cit.riconalla.dentalink.service.UserService;
import org.springframework.web.bind.annotation.*;
import edu.cit.riconalla.dentalink.dto.GoogleLoginRequest;
import edu.cit.riconalla.dentalink.service.GoogleService;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;
    private final GoogleService googleService;

    public AuthController(UserService userService, GoogleService googleService) {
        this.userService = userService;
        this.googleService = googleService;
    }

    @PostMapping("/register")
    public String register(@RequestBody RegisterRequest request) {

        userService.registerUser(request);

        return "User registered successfully";
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {

        String token = userService.login(request);

        return new LoginResponse(token);
    }

    @PostMapping("/google")
    public LoginResponse googleLogin(@RequestBody GoogleLoginRequest request) {

        String token = googleService.loginWithGoogle(request.getIdToken());

        return new LoginResponse(token);
    }
}