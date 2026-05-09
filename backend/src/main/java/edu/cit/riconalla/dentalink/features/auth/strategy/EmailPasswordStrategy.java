package edu.cit.riconalla.dentalink.features.auth.strategy;

import edu.cit.riconalla.dentalink.features.auth.dto.AuthResponseDto;
import edu.cit.riconalla.dentalink.features.auth.dto.LoginRequest;
import edu.cit.riconalla.dentalink.dto.UserDto;
import edu.cit.riconalla.dentalink.features.auth.entity.User;
import edu.cit.riconalla.dentalink.shared.exception.InvalidCredentialsException;
import edu.cit.riconalla.dentalink.features.auth.repository.UserRepository;
import edu.cit.riconalla.dentalink.shared.security.JwtUtil;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class EmailPasswordStrategy implements AuthStrategy {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public EmailPasswordStrategy(UserRepository userRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public AuthResponseDto login(Object request) {
        LoginRequest req = (LoginRequest) request;

        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());

        UserDto userDto = new UserDto(
                user.getUserId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getRole().name(),
                user.getProfileImageUrl()
        );

        return new AuthResponseDto(userDto, token);
    }
}