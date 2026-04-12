package edu.cit.riconalla.dentalink.service;

import edu.cit.riconalla.dentalink.strategy.AuthStrategy;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    public String authenticate(AuthStrategy strategy, Object request) {
        return strategy.login(request);
    }
}