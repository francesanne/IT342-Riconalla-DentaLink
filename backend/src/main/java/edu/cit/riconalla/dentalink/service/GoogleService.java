package edu.cit.riconalla.dentalink.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.http.javanet.NetHttpTransport;

import edu.cit.riconalla.dentalink.entity.User;
import edu.cit.riconalla.dentalink.entity.Role;
import edu.cit.riconalla.dentalink.repository.UserRepository;
import edu.cit.riconalla.dentalink.security.JwtUtil;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;

@Service
public class GoogleService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    private final String CLIENT_ID = "573816851730-23qfd86kjha2ahkv57cdfaoakk2bm4t8.apps.googleusercontent.com";

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
                    .setAudience(Collections.singletonList(CLIENT_ID))
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

    public String loginWithGoogle(String idTokenString) {

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
                    newUser.setPassword("");
                    newUser.setRole(Role.PATIENT);
                    newUser.setCreatedAt(LocalDateTime.now());
                    return userRepository.save(newUser);
                });

        return jwtUtil.generateToken(user.getEmail(), user.getRole().name());
    }
}

//complete