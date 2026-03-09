package com.dentalink.backend.controller;

import com.dentalink.backend.dto.LoginRequest;
import com.dentalink.backend.dto.LoginResponse;
import com.dentalink.backend.dto.RegisterRequest;
import com.dentalink.backend.service.UserService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
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
}