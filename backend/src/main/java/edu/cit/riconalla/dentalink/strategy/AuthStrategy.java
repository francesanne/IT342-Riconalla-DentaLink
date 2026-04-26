package edu.cit.riconalla.dentalink.strategy;
import edu.cit.riconalla.dentalink.dto.AuthResponseDto;

public interface AuthStrategy {
    AuthResponseDto login(Object request);
}