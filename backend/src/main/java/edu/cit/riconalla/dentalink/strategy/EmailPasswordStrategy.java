package edu.cit.riconalla.dentalink.strategy;

import edu.cit.riconalla.dentalink.dto.LoginRequest;
import edu.cit.riconalla.dentalink.entity.User;
import edu.cit.riconalla.dentalink.repository.UserRepository;
import edu.cit.riconalla.dentalink.security.JwtUtil;
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
    public String login(Object request) {
        LoginRequest req = (LoginRequest) request;

        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }

        return jwtUtil.generateToken(
                user.getEmail(),
                user.getRole().name()
        );
    }
}