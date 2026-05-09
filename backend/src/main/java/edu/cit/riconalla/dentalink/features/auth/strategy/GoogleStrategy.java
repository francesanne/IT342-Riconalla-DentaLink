package edu.cit.riconalla.dentalink.features.auth.strategy;

import edu.cit.riconalla.dentalink.features.auth.dto.AuthResponseDto;
import edu.cit.riconalla.dentalink.features.auth.dto.GoogleLoginRequest;
import edu.cit.riconalla.dentalink.features.auth.service.GoogleService;
import org.springframework.stereotype.Component;

@Component
public class GoogleStrategy implements AuthStrategy {

    private final GoogleService googleService;

    public GoogleStrategy(GoogleService googleService) {
        this.googleService = googleService;
    }

    @Override
    public AuthResponseDto login(Object request) {
        GoogleLoginRequest req = (GoogleLoginRequest) request;
        return googleService.loginWithGoogle(req.getIdToken());
    }
}