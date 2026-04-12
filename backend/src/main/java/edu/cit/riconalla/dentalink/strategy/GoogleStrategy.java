package edu.cit.riconalla.dentalink.strategy;

import edu.cit.riconalla.dentalink.dto.GoogleLoginRequest;
import edu.cit.riconalla.dentalink.service.GoogleService;
import org.springframework.stereotype.Component;

@Component
public class GoogleStrategy implements AuthStrategy {

    private final GoogleService googleService;

    public GoogleStrategy(GoogleService googleService) {
        this.googleService = googleService;
    }

    @Override
    public String login(Object request) {
        GoogleLoginRequest req = (GoogleLoginRequest) request;
        return googleService.loginWithGoogle(req.getIdToken());
    }
}