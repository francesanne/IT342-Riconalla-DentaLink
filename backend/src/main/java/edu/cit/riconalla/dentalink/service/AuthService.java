package edu.cit.riconalla.dentalink.service;

import edu.cit.riconalla.dentalink.dto.AuthResponseDto;
import edu.cit.riconalla.dentalink.dto.RegisterRequest;
import edu.cit.riconalla.dentalink.dto.UserDto;
import edu.cit.riconalla.dentalink.entity.User;
import edu.cit.riconalla.dentalink.security.JwtUtil;
import edu.cit.riconalla.dentalink.strategy.AuthStrategy;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;

    public AuthService(UserService userService,
                       JwtUtil jwtUtil,
                       EmailService emailService) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
        this.emailService = emailService;
    }

    public AuthResponseDto authenticate(AuthStrategy strategy, Object request) {
        return strategy.login(request);
    }

    public AuthResponseDto register(RegisterRequest request) {
        User user = userService.registerUser(request);

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());

        UserDto userDto = new UserDto(
                user.getUserId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getRole().name(),
                user.getProfileImageUrl()
        );

        // Send welcome email — SDD §2.4 (mandatory, non-blocking)
        emailService.sendWelcomeEmail(user.getEmail(), user.getFirstName());

        return new AuthResponseDto(userDto, token);
    }
}