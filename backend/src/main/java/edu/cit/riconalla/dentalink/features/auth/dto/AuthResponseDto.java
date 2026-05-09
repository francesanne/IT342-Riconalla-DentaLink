package edu.cit.riconalla.dentalink.features.auth.dto;

import edu.cit.riconalla.dentalink.dto.UserDto;

public class AuthResponseDto {

    private UserDto user;
    private String accessToken;

    public AuthResponseDto(UserDto user, String accessToken) {
        this.user = user;
        this.accessToken = accessToken;
    }

    public UserDto getUser() { return user; }
    public String getAccessToken() { return accessToken; }
}