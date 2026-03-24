package edu.cit.riconalla.dentalink.controller;

import edu.cit.riconalla.dentalink.dto.LoginRequest;
import edu.cit.riconalla.dentalink.dto.LoginResponse;
import edu.cit.riconalla.dentalink.dto.RegisterRequest;
import edu.cit.riconalla.dentalink.service.UserService;
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