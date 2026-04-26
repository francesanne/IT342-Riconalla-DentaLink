package edu.cit.riconalla.dentalink.dto;

public class UserDto {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String role;
    private String profileImageUrl;

    public UserDto(Long id, String firstName, String lastName,
                   String email, String role, String profileImageUrl) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.role = role;
        this.profileImageUrl = profileImageUrl;
    }

    public Long getId() { return id; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public String getProfileImageUrl() { return profileImageUrl; }
}