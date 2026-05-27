package edu.cit.riconalla.dentalink.features.auth.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.http.javanet.NetHttpTransport;

import edu.cit.riconalla.dentalink.features.auth.dto.AuthResponseDto;
import edu.cit.riconalla.dentalink.features.profile.dto.UserDto;
import edu.cit.riconalla.dentalink.features.auth.entity.User;
import edu.cit.riconalla.dentalink.features.auth.entity.Role;
import edu.cit.riconalla.dentalink.features.auth.repository.UserRepository;
import edu.cit.riconalla.dentalink.shared.security.JwtUtil;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class GoogleService {

    @Value("${google.client-id}")
    private String clientId;

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public GoogleService(UserRepository userRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    public GoogleIdToken.Payload verifyToken(String idTokenString) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(),
                    GsonFactory.getDefaultInstance()
            )
                    .setAudience(Collections.singletonList(clientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(idTokenString);

            if (idToken != null) {
                return idToken.getPayload();
            } else {
                throw new RuntimeException("Invalid Google token");
            }

        } catch (Exception e) {
            throw new RuntimeException("Google token verification failed");
        }
    }

    public AuthResponseDto loginWithGoogle(String idTokenString) {
        GoogleIdToken.Payload payload = verifyToken(idTokenString);

        String email = payload.getEmail();
        String firstName = (String) payload.get("given_name");
        String lastName = (String) payload.get("family_name");
        String googleId = payload.getSubject();

        User user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setEmail(email);
                    newUser.setFirstName(firstName);
                    newUser.setLastName(lastName);
                    newUser.setGoogleId(googleId);
                    newUser.setPassword(null);  // OAuth users have no password
                    newUser.setRole(Role.PATIENT);
                    return userRepository.save(newUser);
                });

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