package edu.cit.riconalla.dentalink.features.auth.strategy;
import edu.cit.riconalla.dentalink.features.auth.dto.AuthResponseDto;

public interface AuthStrategy {
    AuthResponseDto login(Object request);
}