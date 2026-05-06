package edu.cit.riconalla.dentalink.dto;

public class UpdateProfileRequest {

    private String firstName;
    private String lastName;
    private String currentPassword;
    private String newPassword; // optional

    public String getFirstName()                    { return firstName; }
    public void setFirstName(String firstName)      { this.firstName = firstName; }

    public String getLastName()                     { return lastName; }
    public void setLastName(String lastName)        { this.lastName = lastName; }

    public String getCurrentPassword()                      { return currentPassword; }
    public void setCurrentPassword(String currentPassword)  { this.currentPassword = currentPassword; }

    public String getNewPassword()                  { return newPassword; }
    public void setNewPassword(String newPassword)  { this.newPassword = newPassword; }
}